package com.lxf.ndkdemo.update;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.lxf.ndkdemo.base.RxBus;

import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class MyIntentService extends IntentService {
    private static final String ACTION_DOWNLOAD = "cn.izis.yzext.service.action.download";

    private static final String DOWNLOAD_URL = "downloadUrl";
    private static final String APK_PATH = "apkPath";

    private CompositeDisposable cd = new CompositeDisposable();
//    private NotificationCompat.Builder builder;
//    private NotificationManager notificationManager;

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startUpdateService(Context context, String url, String apkPath) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(DOWNLOAD_URL, url);
        intent.putExtra(APK_PATH, apkPath);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
//                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                builder = new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle("开始下载")
//                        .setAutoCancel(true)
//                        .setContentText("版本更新");
//
//                notificationManager.notify(0, builder.build());
                System.out.println("MyIntentService.onHandleIntent:开始下载");

                String url = intent.getStringExtra(DOWNLOAD_URL);
                String apkPath = intent.getStringExtra(APK_PATH);
                handleUpdate(url, apkPath);
            }
        }
    }

    private void handleUpdate(String url, String apkPath) {
        subscribeEvent();
        UpdateManager.downloadApk(this, url, apkPath, cd);
    }

    private void subscribeEvent() {
        RxBus.getDefault().toObservable(FileLoadingBean.class)
                .subscribe(new Observer<FileLoadingBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("MyIntentService.onSubscribe");
                        cd.add(d);
                    }

                    @Override
                    public void onNext(FileLoadingBean fileLoadingBean) {
                        int progress = (int) Math.round(fileLoadingBean.getBytesReaded() / (double) fileLoadingBean.getTotal() * 100);
//                        builder.setContentInfo(String.valueOf(progress) + "%").setProgress(100, progress, false);
//                        notificationManager.notify(0, builder.build());
//
//                        if (progress == 100)
//                            notificationManager.cancel(0);
                        System.out.println("MyIntentService.onNext:" + progress + "%");
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscribeEvent();
                    }

                    @Override
                    public void onComplete() {
                        subscribeEvent();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("onDestory____MyIntentService");
    }
}
