package com.izis.yzext;

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
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.izis.yzext.helper.TileHelper;
import com.izis.yzext.net.NetKt;
import com.izis.yzext.net.NetWorkSoap;
import com.izis.yzext.net.ProgressSubscriber;
import com.izis.yzext.pl2303.ActivityCallBridge;
import com.izis.yzext.pl2303.IPL2303ConnectSuccess;
import com.izis.yzext.pl2303.LiveType;
import com.izis.yzext.pl2303.Pl2303InterfaceUtilNew;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import lxf.widget.tileview.Board;

import static com.izis.yzext.PaserUtil.BOARD_SIZE;


public class MyService extends Service implements ActivityCallBridge.PL2303Interface {
    private static final boolean DEBUG = true;

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
    private int rectIndex;//当前选择的是第5个外轮廓
    private boolean startCapture;
    private boolean firstRect = true;//第一次自动识别

    private String preChess;//上一帧数据
    private String currChess;//当前帧数据
    private int preChessBW = 2;//最后手落子颜色 1黑2白
    private TileHelper tileHelper;
    private float startX, startY;
    private int[] location = new int[2];
    private boolean isFirst = true;//是否是截屏开始的第一帧数据
    private GameInfo game;

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
        otherVisibility(false);

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
                    otherVisibility(false);
                    wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

                    wmParams.x = w - mFloatView.getMeasuredWidth();
                    wmParams.y = h - 110;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);

                } else {
                    otherVisibility(true);
                    wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

                    wmParams.x = 0;
                    wmParams.y = 0;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                    if (mPathView.getRectF().width() == 0)
                        btnFindRect.performClick();
                }
            }
        });

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startCapture = isChecked;
                if (isChecked) {
//                    SharedPreferences sharedPreferences = getSharedPreferences("lxf_path", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putInt("width", (int) mPathView.getRectF().width());
//                    editor.putInt("left", (int) mPathView.getRectF().left);
//                    editor.putInt("top", (int) mPathView.getRectF().top);
//                    editor.apply();

//                    if (tileHelper!=null && tileHelper.isConnected()){
//                        mFloatView.performClick();
//                        interval();
//                        startVirtual();
//                    }else{
//                        btnConnect.performClick();
//                    }
                    btnConnect.performClick();

                    mPathView.moveable = false;
                } else {
                    dispose();
                    stopVirtual();
                    if (tileHelper != null && tileHelper.isConnected())
//                        tileHelper.disConnect();
                        tileHelper.onDestroy();
                    mPathView.moveable = true;
                    mFloatView.performClick();
//                    mPathView.getRectF().set(new RectF(0, 0, 0, 0));
                    preChess = null;
                    isFirst = true;

                    uploadChess();
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
                        PaserUtil.BOARD_SIZE = game.getBoardSize();
                        Board.n = game.getBoardSize();

//                        final Pl2303InterfaceUtilNew pl2303 = Pl2303InterfaceUtilNew.initInterface(MyService.this,
//                                game.getNextBW() == 1 ? "+" : "-", MyService.this);//game.getNextBW()这里这个实际是没用的
                        final Pl2303InterfaceUtilNew pl2303 = Pl2303InterfaceUtilNew.initInterface(MyService.this,
                                "+", MyService.this);
                        pl2303.setIpl2303ConnectSuccess(new IPL2303ConnectSuccess() {
                            @Override
                            public void openSuccess() {
                                pl2303.WriteToUARTDevice("~CTS0#");

                                mFloatView.performClick();
                                interval();
                                startVirtual();
                            }
                        });
                        tileHelper = new TileHelper(pl2303, game);
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
                AppUtil.killApp(MyService.this, ServiceActivity.getPLATFORM());
                AppUtil.killApp(MyService.this, "com.izis.yzext");
                stopSelf();
            }
        });
        btnFindRect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rects == null) {
                    startVirtual();
                } else {
                    rectIndex++;
                    if (rectIndex > rects.size() - 1)
                        rectIndex = 0;
                    updatePath(rectIndex);
                }
            }
        });
    }

    private void uploadChess() {
        if (tileHelper != null && tileHelper.isNormalChess() && ServiceActivity.getBoardId() != null) {
            AlertDialog dialog = new AlertDialog.Builder(MyService.this, R.style.dialog)
                    .setTitle("保存棋谱")
                    .setMessage("棋谱保存后可以在标准版应用中查询，是否保存当前对局棋谱?")
                    .setPositiveButton("保存", new DialogInterface.OnClickListener() {
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
                                                    "第三方对弈",
                                                    game.getBw() == 1 ? "自己" : "三方棋友",
                                                    game.getBw() == 1 ? "三方棋友" : "自己",
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
                                    .subscribe(new ProgressSubscriber<String>(getApplicationContext(), true, false, "正在提交", WindowManager.LayoutParams.TYPE_SYSTEM_ALERT) {
                                        @Override
                                        public void _onError(@Nullable String error) {
                                        }

                                        @Override
                                        public void _onNext(String s) {
                                            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
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
        if (rects.size() <= index) return;
        if (index >= 0) {
            Rect rect = rects.get(index);
            log("当前选择区域：" + rect);
            mPathView.setRectF(new RectF(rect.x, rect.y - statusH, rect.x + rect.width, rect.y + rect.height - statusH));
        } else {
            switch (ServiceActivity.Companion.getPLATFORM()) {
                case ServiceActivityKt.PLATFORM_XB:
                    mPathView.setRectF(new RectF(31, 11, 489, 469));
                    break;
                default:
                    mPathView.setRectF(new RectF(100, 100, 300, 300));
                    break;
            }
        }
    }

    private void otherVisibility(boolean visibility) {
        mPathView.setVisibility(visibility ? View.VISIBLE : View.GONE);
        mToggleButton.setVisibility(visibility ? View.VISIBLE : View.GONE);
//        btnConnect.setVisibility(visibility ? View.VISIBLE : View.GONE);
        btnExit.setVisibility(visibility ? View.VISIBLE : View.GONE);
//        btnFindRect.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    private void interval() {
        disposable = Flowable.interval(3000, 3000, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        startVirtual();
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
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
//                log("onImageAvailable");
                stopVirtual();

                Bitmap bitmap = startCapture();
//                FileUtil.save(bitmap);
//                log("截图：" + bitmap.getWidth() + ";" + bitmap.getHeight());

                if (!startCapture) { //第一次选区
                    rects = PaserUtil.findRects(bitmap);
                    System.out.println(Arrays.toString(rects.toArray()));
                    rectIndex = rects.size() - 1;
//                    Observable.just(1)
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Consumer<Integer>() {
//                                @Override
//                                public void accept(Integer integer) throws Exception {
//                                    Toast.makeText(MyService.this, "找到可能区域：" + rects.size() + "个", Toast.LENGTH_SHORT).show();
//                                }
//                            });
                    if (firstRect) {
                        updatePath(rectIndex);
                        firstRect = false;
                    }
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
                if (mRectF.top < 0)
                    mRectF.top = 0;
                if (mRectF.bottom < 0)
                    mRectF.bottom = 0;
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

//                    if (game.getType() == 2 && isFirst) {
//                        isFirst = false;
//                        preChess = result;
//                        tileHelper.updateBoard(a);
//                        tileHelper.updateCurBW(game.getNextBW());
//                        log("第一帧数据");
//                    }
                    if (isFirst) {
                        isFirst = false;
                        preChess = result;
                        if (result.contains("1") || result.contains("2")) {
                            tileHelper.updateBoard(a);
                            tileHelper.updateCurBW(game.getBw());//规定只有轮到自己落子才能开启服务
                        }
                        log("第一帧数据");
                    }

                    LiveType liveType = Util.parse(preChess, currChess, 90);//对比屏幕
                    switch (liveType.getType()) {
                        case LiveType.NORMAL:
                            preChess = result;
//                            if ((game.getBw() == 1 && liveType.getAllStep().startsWith("+"))
//                                    || (game.getBw() == 2 && liveType.getAllStep().startsWith("-")))
//                                return;
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
                                    rotate = 0;
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
//                                //一般是电脑下的快，所以先落自己的
//                                tileHelper.putChess(self + other);
                            } else {
                                tileHelper.lamb(allStep, false, rotate);
                                tileHelper.putChess(allStep);

                                preChessBW = allStep.contains("+") ? 1 : 2;
                            }
                            break;
                    }
                }
            }
        }, null);
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

            mediaW = w + nativeH;
            mediaH = h;
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

    public Bitmap startCapture() {
        Image image = mImageReader.acquireNextImage();

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
}
