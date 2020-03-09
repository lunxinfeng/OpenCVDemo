package com.izis.yzext;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.izis.yzext.bean.ChessPosition;
import com.izis.yzext.helper.TileHelper;
import com.izis.yzext.net.NetKt;
import com.izis.yzext.net.NetWorkSoap;
import com.izis.yzext.net.ProgressSubscriber;
import com.izis.yzext.pl2303.ActivityCallBridge;
import com.izis.yzext.pl2303.ChessChange;
import com.izis.yzext.pl2303.IPL2303ConnectSuccess;
import com.izis.yzext.pl2303.LiveType;
import com.izis.yzext.pl2303.Pl2303InterfaceUtilNew;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import lxf.widget.tileview.Board;

import static com.izis.yzext.PaserUtil.BOARD_SIZE;


public class MyService extends Service implements ActivityCallBridge.PL2303Interface {
    private static final boolean DEBUG = true;
    public static boolean TILE_ERROR = false;//棋盘可能出现错误
    private List<ChessPosition> chessPostionList = new ArrayList<>();

    //定义浮动窗口布局
    private ConstraintLayout mFloatLayout;
    private WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    private WindowManager mWindowManager;

    private Button mFloatView;
    private Button btnConnect;
    private PathView mPathView;
    private Button btnExit;
    private Button btnFindRect;
    private ToggleButton mToggleButton;
    //屏幕宽高
    private int w;
    private int h;
    public static int statusH;
    private int nativeH;
    //视频流的宽高
    private int mediaW;
    private int mediaH;

    private ImageReader mImageReader;
    private MediaProjectionManager mMediaProjectionManager;
    private int mScreenDensity;

    public static int mResultCode = 0;
    public static Intent mResultData = null;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private RectF mRectF;//截图区域
    private Disposable disposable;
    private List<Rect> rects;//自动识别出来的最大的5个外轮廓
    private int rectIndex;//当前选择的是最大的外轮廓

    private String preChess;//上一帧数据
    private String currChess;//当前帧数据
    private int preChessBW = 2;//最后手落子颜色 1黑2白
    private TileHelper tileHelper;
    private float startX, startY;
    private int[] location = new int[2];
    private boolean isFirst = true;//是否是截屏开始的第一帧数据
    private boolean isRanged = false;//是否已有合理的选区
    private GameInfo game;
    private boolean clickFromUser = true;//是否人点击选区按钮

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getScreenDimen();


        staticLoadCVLibraries();

        createFloatView();

        createVirtualEnvironment();
    }

    private void getScreenDimen() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (mWindowManager != null) {
            mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        }
        w = outMetrics.widthPixels;
        h = outMetrics.heightPixels;
        log("屏幕尺寸：w：" + w + "；h：" + h);

//        statusH = ScreenUtil.getStatusBarHeight(this);
//        log("状态栏高度：" + statusH);
        nativeH = ScreenUtil.getNavigationBarHeight(this);
        log("虚拟按键高度：" + nativeH);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        log("onConfigurationChanged");
        if (mFloatLayout != null)
            mWindowManager.removeView(mFloatLayout);
        getScreenDimen();
        createFloatView();
        createVirtualEnvironment();
    }

    public TileHelper getTileHelper() {
        return tileHelper;
    }

    @Override
    public void onDestroy() {
        if (mFloatLayout != null) {
            //移除悬浮窗口
            mWindowManager.removeView(mFloatLayout);
        }
        dispose();
        if (tileHelper != null)
            tileHelper.onDestroy();
        super.onDestroy();
        System.out.println("MyService.onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private OnErrorListener errorListener = new OnErrorListener() {
        @Override
        public void onError(final ChessChange chessChange) {
            //停止截图
            dispose();
            mFloatView.post(new Runnable() {
                @Override
                public void run() {
                    //显示透明层
                    if (mPathView.getVisibility() == View.GONE) {
                        clickFromUser = false;
                        mFloatView.performClick();
                    }
                    //绘制错误点
                    mPathView.clearErrorPoint();
                    mPathView.drawErrorPoint(chessChange, game.getBoardSize());
                }
            });

        }

        @Override
        public void onErrorList(final List<ChessChange> chessChangeList) {
            //停止截图
            dispose();
            mFloatView.post(new Runnable() {
                @Override
                public void run() {
                    //显示透明层
                    if (mPathView.getVisibility() == View.GONE) {
                        clickFromUser = false;
                        mFloatView.performClick();
                    }
                    //绘制错误点
                    mPathView.clearErrorPoint();
                    mPathView.drawErrorPoint(chessChangeList, game.getBoardSize());
                }
            });
        }

        @Override
        public void onSuccess() {
            mFloatView.post(new Runnable() {
                @Override
                public void run() {
                    if (disposable == null || disposable.isDisposed()) {
                        mPathView.clearErrorPoint();
                        mPathView.invalidate();
                        if (mPathView.getVisibility() == View.VISIBLE)
                            mFloatView.performClick();
                        interval(200);
//                        startVirtual();
                    }
                }
            });
        }
    };

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        //设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (ConstraintLayout) inflater.inflate(R.layout.path, null);
        //浮动窗口按钮
        mFloatView = mFloatLayout.findViewById(R.id.float_id);
        mPathView = mFloatLayout.findViewById(R.id.pathView);
        btnConnect = mFloatLayout.findViewById(R.id.btnConnect);
        mToggleButton = mFloatLayout.findViewById(R.id.toggleButton);
        btnExit = mFloatLayout.findViewById(R.id.btnExit);
        btnFindRect = mFloatLayout.findViewById(R.id.btnFindRect);
        btnFindRect.setVisibility(View.GONE);
        btnConnect.setVisibility(View.GONE);
        otherVisibility(false, clickFromUser);

        //设置x、y初始值，相对于gravity
        wmParams.x = w - mFloatView.getMeasuredWidth();
        wmParams.y = h - 100;
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        //设置监听浮动窗口的触摸移动
        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mFloatView.getLocationOnScreen(location);
                //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                wmParams.x = (int) event.getRawX() - mFloatView.getMeasuredWidth() / 2;
                //减25为状态栏的高度
                wmParams.y = (int) event.getRawY() - mFloatView.getMeasuredHeight() / 2 - 25;
                //刷新
                mWindowManager.updateViewLayout(mFloatLayout, wmParams);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getRawX();
                    startY = event.getRawY();
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (Math.abs(event.getRawX() - startX) > 50 ||
                            Math.abs(event.getRawY() - startY) > 50) {
//                        return true;
                    } else {
                        mFloatView.performClick();
//                        return false;
                    }
                }
                return true;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

        mFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wmParams.width != WindowManager.LayoutParams.WRAP_CONTENT) {
                    otherVisibility(false, clickFromUser);
                    wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                    wmParams.x = w - mFloatView.getMeasuredWidth();
                    wmParams.y = h - 110;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                } else {
                    otherVisibility(true, clickFromUser);
                    wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

                    wmParams.x = 0;
                    wmParams.y = 0;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
//                    if (mPathView.getRectF().width() == 0)
                    if (clickFromUser && !isRanged)
                        btnFindRect.performClick();
                }
                clickFromUser = true;
            }
        });

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnConnect.performClick();
                    mPathView.moveable = false;
                } else {
                    dispose();

                    Observable.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Long>() {
                                @Override
                                public void accept(Long aLong) throws Exception {
                                    if (tileHelper != null && tileHelper.isConnected())
                                        tileHelper.disConnect();
                                    mPathView.moveable = true;
                                    mFloatView.performClick();
                                    preChess = null;
                                    isFirst = true;
                                    isHanding = false;
                                    TILE_ERROR = false;
                                    mPathView.clearErrorPoint();
                                    mPathView.invalidate();

//                                    if (mImageReader != null) {
//                                        mImageReader.setOnImageAvailableListener(null, null);
//                                    }

                                    uploadChess();
                                }
                            });
                }
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingDialog dialog = new SettingDialog(MyService.this, R.style.dialog);
                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                dialog.setListener(new SettingDialog.OnClickListener() {
                    @Override
                    public void onPositive(@NotNull GameInfo gameInfo) {
                        game = gameInfo;

                        ChessPosition chessPosition = new ChessPosition();
                        chessPosition.setPackageName(ServiceActivity.getPLATFORM());
                        chessPosition.setClassName(KotlinExtKt.getClassName());
                        chessPosition.setBoardSize(game.getBoardSize());
                        chessPosition.setLeft((int) mPathView.getRectF().left);
                        chessPosition.setTop((int) mPathView.getRectF().top);
                        chessPosition.setSize((int) mPathView.getRectF().width());
                        chessPostionList.remove(chessPosition);
                        chessPostionList.add(chessPosition);

                        PaserUtil.BOARD_SIZE = game.getBoardSize();
                        Board.n = game.getBoardSize();
                        final Pl2303InterfaceUtilNew pl2303 = Pl2303InterfaceUtilNew.initInterface(MyService.this,
                                "+", MyService.this);
                        pl2303.setIpl2303ConnectSuccess(new IPL2303ConnectSuccess() {
                            @Override
                            public void openSuccess() {
                                pl2303.WriteToUARTDevice("~CTS0#");

                                mFloatView.performClick();

                                isFirst = true;
                                interval(2000);
//                                startVirtual();
                            }
                        });
                        tileHelper = new TileHelper(pl2303, game, errorListener);
                        tileHelper.connect(null);
                    }
                });
                dialog.show();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVirtual();
                if (tileHelper != null)
                    tileHelper.onDestroy();
                SystemClock.sleep(100);
                AppUtil.killApp(MyService.this, ServiceActivity.getPLATFORM());
                AppUtil.killApp(MyService.this, "com.izis.yzext");
                stopSelf();
            }
        });
        btnFindRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVirtual();
//                if (rects == null) {
//                    startVirtual();
//                } else {
//                    rectIndex++;
//                    if (rectIndex > rects.size() - 1)
//                        rectIndex = 0;
//                    updatePath(rectIndex);
//                }
            }
        });
    }

    private void uploadChess() {
        if (tileHelper != null && tileHelper.isNormalChess() && ServiceActivity.getBoardId() != null) {
            AlertDialog dialog = new AlertDialog.Builder(MyService.this, R.style.dialog)
                    .setTitle(getResources().getString(R.string.save_chess_title))
                    .setMessage(getResources().getString(R.string.save_chess_message))
                    .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NetWorkSoap.Companion.getInstance()
                                    .postSoap(
                                            NetWorkSoap.Companion.getMETHOD_NAME_UPDATE(),
                                            NetKt.getRobot_result(),
                                            NetKt.robotResult(
                                                    tileHelper.sgf(),
                                                    tileHelper.sgf().length() / 5,
                                                    game.getBoardSize(),
                                                    getResources().getString(R.string.third_party_game),
                                                    game.getBw() == 1 ? getResources().getString(R.string.self) : getResources().getString(R.string.third_party_player),
                                                    game.getBw() == 1 ? getResources().getString(R.string.third_party_player) : getResources().getString(R.string.self),
                                                    "",
                                                    "",
                                                    "",
                                                    200
                                            ),
                                            ServiceActivity.getBoardId(),
                                            String.class
                                    )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new ProgressSubscriber<String>(getApplicationContext(), true, false, getResources().getString(R.string.submitting), WindowManager.LayoutParams.TYPE_SYSTEM_ALERT) {
                                        @Override
                                        public void _onError(@Nullable String error) {
                                        }

                                        @Override
                                        public void _onNext(String s) {
                                            try {
                                                if (Integer.parseInt(s) > 0)
                                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.not_save), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();
        }
    }

    private void updatePath(int index) {
        if (index >= 0 && rects.size() > index) {
            Rect rect = rects.get(index);
            log("当前选择区域：" + rect);
            if (rect.width >= 460 || rect.width < 350) {//棋盘尺寸应该在350 ~ 460之间，实际经验
                updatePath(index - 1);
            } else {
                if (Math.abs(rect.width - rect.height) < 5) {
                    isRanged = true;
                    mPathView.setRectF(new RectF(rect.x, rect.y - statusH, rect.x + rect.width, rect.y + rect.height - statusH));
                } else {
                    //使用缓存的位置
                    useCachePath();
                }
            }
        } else {
            //使用缓存的位置
            useCachePath();
        }
    }

    private void useCachePath() {
        ChessPosition chessPosition = null;
        for (ChessPosition c : chessPostionList) {
            if (c.getPackageName().equals(ServiceActivity.getPLATFORM()) && c.getClassName().equals(KotlinExtKt.getClassName()) && c.getBoardSize() == BOARD_SIZE) {
                chessPosition = c;
                break;
            }
        }
        if (chessPosition != null) {
            Rect rect = new Rect(chessPosition.getLeft(), chessPosition.getTop(), chessPosition.getSize(), chessPosition.getSize());
            isRanged = true;
            mPathView.setRectF(new RectF(rect.x, rect.y - statusH, rect.x + rect.width, rect.y + rect.height - statusH));
        } else {
            mFloatView.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyService.this.getApplicationContext(), getResources().getString(R.string.select_hint), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void otherVisibility(boolean visibility, boolean clickFromUser) {
        mPathView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        mToggleButton.setVisibility(visibility && clickFromUser ? View.VISIBLE : View.GONE);
//        btnConnect.setVisibility(visibility ? View.VISIBLE : View.GONE);
        btnExit.setVisibility(visibility && clickFromUser ? View.VISIBLE : View.GONE);
//        btnFindRect.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void interval(long initTime) {
        disposable = Flowable.interval(initTime, 300, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) {
                        return !isHanding;
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) {
//                        startVirtual();
                        handleCapture();
                    }
                });
    }

    private void dispose() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }

    private void log(String info) {
        if (DEBUG)
            System.out.println(info);
    }

    private void createVirtualEnvironment() {
        mMediaProjectionManager = ((ShotApplication) getApplication()).getmMediaProjectionManager();
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mImageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 1);
    }

    @Override
    public void ReadData(String readdata) {
        tileHelper.readData(readdata);
    }

    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("MyService", "Open CV Libraries loaded...");
        } else {
            Log.i("MyService", "Open CV Libraries not loaded...");
        }
    }

    public void startVirtual() {
        screenOrientation();
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }

//        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                isHanding = true;
//                mImageReader.setOnImageAvailableListener(null, null);
//                Bitmap bitmap = startCapture();
//                if (!startCapture && bitmap != null) { //第一次选区
//                    rects = PaserUtil.findRects(bitmap);
//                    System.out.println(Arrays.toString(rects.toArray()));
//                    rectIndex = rects.size() - 1;
//                    if (firstRect) {
//                        updatePath(rectIndex);
//                        firstRect = false;
//                    }
//                }
//                isHanding = false;
//            }
//        }, null);


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isHanding = true;
        Bitmap bitmap = startCapture();

        if (bitmap != null) {
            rects = PaserUtil.findRects(bitmap);
            System.out.println(Arrays.toString(rects.toArray()));
            rectIndex = rects.size() - 1;
            updatePath(rectIndex);
        }

        isHanding = false;
    }

    private void screenOrientation() {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            int max = Math.max(w, h);
            int min = Math.min(w, h);
            h = min;
            w = max;

            if (Build.VERSION.SDK_INT < 24) {
                mediaW = w + nativeH;
                mediaH = h;
            } else {
                mediaW = w;
                mediaH = 480;
            }
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            int max = Math.max(w, h);
            int min = Math.min(w, h);
            w = min;
            h = max;

            mediaW = w;
            mediaH = h + nativeH;
        }
        log("视频流宽度：" + mediaW);
        log("视频流高度：" + mediaH);
    }

    public void stopVirtual() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
    }

    private void setUpMediaProjection() {
        mResultData = ((ShotApplication) getApplication()).getIntent();
        mResultCode = ((ShotApplication) getApplication()).getResult();
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mediaW, mediaH, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private Bitmap startCapture() {
        Image image = mImageReader.acquireNextImage();
        if (image == null)
            return null;
        int width = image.getWidth();
        int height = image.getHeight();

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();

        return bitmap;
    }

    private boolean isHanding = false;

    public void handleCapture() {
        isHanding = true;

        Bitmap bitmap = startCapture();
//                FileUtil.save(bitmap);
//                log("截图：" + bitmap.getWidth() + ";" + bitmap.getHeight());


        if (TILE_ERROR || bitmap == null) {//error状态不处理
            isHanding = false;
            return;
        }

        mRectF = new RectF(mPathView.getRectF());
        log("原始区域：" + mRectF);
        //选框扩大0.5个格子
        float width = mRectF.width();
        mRectF.left = mRectF.left - width / (BOARD_SIZE - 1) / 2;
        mRectF.right = mRectF.right + width / (BOARD_SIZE - 1) / 2;
        mRectF.top = mRectF.top - width / (BOARD_SIZE - 1) / 2 + statusH;
        mRectF.bottom = mRectF.bottom + width / (BOARD_SIZE - 1) / 2 + statusH;

        if (mRectF.left < 0)
            mRectF.left = 0;
        if (mRectF.right < 0)
            mRectF.right = 0;
        if (mRectF.right > 479.9 && ScreenUtil.isPortrait(this))
            mRectF.right = 479.9f;
        if (mRectF.top < 0)
            mRectF.top = 0;
        if (mRectF.bottom < 0)
            mRectF.bottom = 0;
        if (mRectF.bottom > 750 && ScreenUtil.isPortrait(this))
            mRectF.bottom = 750;
        int cut_width = (int) Math.abs(mRectF.right - mRectF.left);
        int cut_height = (int) Math.abs(mRectF.bottom - mRectF.top);
        log("转换区域：" + mRectF);

        tileHelper.setRectF(mRectF);

        if (cut_width > 0 && cut_height > 0) {
            if (mRectF.left + cut_width > bitmap.getWidth()) {
                cut_width = bitmap.getWidth() - (int) mRectF.left;
            }
            if (mRectF.top + cut_height > bitmap.getHeight()) {
                cut_height = bitmap.getHeight() - (int) mRectF.top;
            }
            Bitmap cutBitmap = Bitmap.createBitmap(bitmap, (int) mRectF.left, (int) mRectF.top, cut_width, cut_height);
//                    FileUtil.save(cutBitmap);
            int[][] a = PaserUtil.parse(cutBitmap);
            String result = PaserUtil.exChange(a);
            log(result);
            currChess = result;

            if (isFirst) {
                isFirst = false;
                preChess = result;
                if (result.contains("1") || result.contains("2")) {
                    int count = 0;
                    for (int i = 0; i < result.length(); i++) {
                        char c = result.charAt(i);
                        if (c != '0') {
                            count++;
                        }
                    }

                    if (count > 1) {
                        log("重新给board赋值");
                        tileHelper.updateBoard(a);
                        tileHelper.updateCurBW(game.getBw());//规定只有轮到自己落子才能开启服务
                    } else {
                        //如果只有一颗子,不要直接替换board二维数组,调用显示棋步的方法
                        preChess = "";
                        for (int i = 0; i < game.getBoardSize(); i++) {
                            for (int j = 0; j < game.getBoardSize(); j++) {
                                preChess = preChess + "0";
                            }
                        }
                    }
                }
                log("第一帧数据");
            }

            LiveType liveType = Util.parse(preChess, currChess, 90);//对比屏幕
            switch (liveType.getType()) {
                case LiveType.NORMAL:
                    preChess = result;

                    String allStep = liveType.getAllStep();
                    log("正常操作：" + allStep + "；序列：" + liveType.getIndex());
                    int rotate = 270;
                    switch (ServiceActivity.Companion.getPLATFORM()) {
                        case ServiceActivityKt.PLATFORM_TX:
                        case ServiceActivityKt.PLATFORM_YC:
                        case ServiceActivityKt.PLATFORM_YK:
//                                case ServiceActivityKt.PLATFORM_YZ:
                            rotate = 270;
                            break;
                        case ServiceActivityKt.PLATFORM_XB:
                        case ServiceActivityKt.PLATFORM_JJ:
                            rotate = Build.VERSION.SDK_INT >= 24 ? 180 : 0;
                            break;
                    }

                    if (allStep.length() == 10) {
                        String self;
                        String other;
                        if (game.getBw() == 1) {//自己执黑
                            if (allStep.startsWith("+")) {
                                self = allStep.substring(0, 5);//自己
                                other = allStep.substring(5, 10);//对手
                            } else {
                                self = allStep.substring(5, 10);//自己
                                other = allStep.substring(0, 5);//对手
                            }

                            if (preChessBW == 2)
                                tileHelper.putChess(self + other);
                            else
                                tileHelper.putChess(other + self);
                        } else {
                            if (allStep.startsWith("-")) {
                                self = allStep.substring(0, 5);//自己
                                other = allStep.substring(5, 10);//对手
                            } else {
                                self = allStep.substring(5, 10);//自己
                                other = allStep.substring(0, 5);//对手
                            }

                            if (preChessBW == 1)
                                tileHelper.putChess(self + other);
                            else
                                tileHelper.putChess(other + self);
                        }
                        tileHelper.lamb(other, false, rotate);
                    } else {
                        tileHelper.lamb(allStep, false, rotate);
                        tileHelper.putChess(allStep);

                        preChessBW = allStep.contains("+") ? 1 : 2;
                    }
                    break;
            }
        }
        isHanding = false;
    }
}
