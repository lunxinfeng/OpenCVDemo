//package com.izis.yzext.update;
//
//import android.app.AlertDialog;
//import android.app.IntentService;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.ProgressBar;
//
//import com.izis.yzext.base.RxBus;
//import com.izis.yzext.net.FileLoadingBean;
//
//import io.reactivex.Observer;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//
///**
// * An {@link IntentService} subclass for handling asynchronous task requests in
// * a service on a separate handler thread.
// * <p>
// */
//public class MyIntentService extends IntentService {
//    private static final String ACTION_DOWNLOAD = "cn.izis.yzext.service.action.download";
//
//    private static final String DOWNLOAD_URL = "downloadUrl";
//    private static final String APK_PATH = "apkPath";
//
//    //    private CompositeDisposable cd = new CompositeDisposable();
////    private NotificationCompat.Builder builder;
////    private NotificationManager notificationManager;
//    private static ProgressDialog progressDialog;
//    private UpdateManager downloadManager = new UpdateManager();
//
//
//    public MyIntentService() {
//        super("MyIntentService");
//    }
//
//    public static void startUpdateService(Context context, String url, String apkPath, ProgressDialog dialog) {
//        progressDialog = dialog;
//
//        Intent intent = new Intent(context, MyIntentService.class);
//        intent.setAction(ACTION_DOWNLOAD);
//        intent.putExtra(DOWNLOAD_URL, url);
//        intent.putExtra(APK_PATH, apkPath);
//        context.startService(intent);
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            String action = intent.getAction();
//            if (ACTION_DOWNLOAD.equals(action)) {
////                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
////                builder = new NotificationCompat.Builder(this)
////                        .setSmallIcon(R.mipmap.ic_launcher)
////                        .setContentTitle("开始下载")
////                        .setAutoCancel(true)
////                        .setContentText("版本更新");
////
////                notificationManager.notify(0, builder.build());
//                System.out.println("MyIntentService.onHandleIntent:开始下载");
//
//                String url = intent.getStringExtra(DOWNLOAD_URL);
//                String apkPath = intent.getStringExtra(APK_PATH);
//                handleUpdate(url, apkPath);
//            }
//        }
//    }
//
//    private void handleUpdate(String url, final String apkPath) {
//        downloadManager.setListener(new UpdateManager.DownloadListener() {
//            @Override
//            public void onProgress(int progress) {
//                if (progressDialog != null && progressDialog.isShowing() && progress > progressDialog.getProgress())
//                    progressDialog.updateProgress(progress);
//            }
//
//            @Override
//            public void onComplete() {
//                downloadManager.installApk(apkPath,MyIntentService.this);
//            }
//
//            @Override
//            public void onFail() {
//                new AlertDialog.Builder()
//            }
//        });
//        downloadManager.downloadApk(url, apkPath);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        System.out.println("onDestory____MyIntentService");
//    }
//}
