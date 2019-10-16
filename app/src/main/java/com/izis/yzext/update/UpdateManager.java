package com.izis.yzext.update;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.izis.yzext.base.RxBus;
import com.izis.yzext.net.FileLoadingBean;
import com.izis.yzext.net.NetWork;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * app更新管理类
 * Created by lxf on 2017/3/3.
 */
public class UpdateManager {

    private Disposable disposableDownload;
    private Disposable disposableListener;
    public long downloadLength;
    public long totalLength;

    private DownloadListener listener;

    public UpdateManager() {
        RxBus.getDefault().toObservable(FileLoadingBean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FileLoadingBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableListener = d;
                    }

                    @Override
                    public void onNext(FileLoadingBean value) {
                        int progress =
                                (int) Math.round((value.getBytesReaded() + downloadLength) / (double) totalLength * 100);
                        if (listener != null)
                            listener.onProgress(progress);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (disposableListener != null)
                            disposableListener.dispose();
                    }

                    @Override
                    public void onComplete() {
                        if (disposableListener != null)
                            disposableListener.dispose();
                    }
                });
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public interface DownloadListener {
        void onProgress(int progress);

        void onComplete();

        void onFail();
    }

    /**
     * 是否需要更新,需要则下载
     *
     * @param url               新版本地址
     * @param apkPath           本地apk保存路径
//     * @param versionCodeServer 服务器的版本号，如果不需要验证传-1
     */
    public void downloadApk(final String url, final String apkPath) {
        NetWork.Companion.getInstance()
                .fileLength(url)
//                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {and
//                    @Override
//                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) {
//                        System.out.println("重试");
//                        return Observable.timer(2000, TimeUnit.MILLISECONDS);
//                    }
//                })
                .map(new Function<ResponseBody, Long>() {
                    @Override
                    public Long apply(ResponseBody responseBody) {
                        if (responseBody == null)
                            return 0L;
                        totalLength = responseBody.contentLength();
                        System.out.println("请求文件长度：" + totalLength);
                        return totalLength;
                    }
                })
                .flatMap(new Function<Long, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(Long contentLength) {
                        if (contentLength == 0) {
                            return Observable.error(new RuntimeException("请求异常"));
                        }
                        File file = new File(apkPath);
                        if (!file.exists()) {//文件不存在
                            downloadLength = 0;
                        } else {//文件存在
                            //下载中断时不完整的apk无法获取版本号，验证版本号是否最新这种方法无效，只能每次新版本名字不同
//                            if (versionCodeServer != -1) {//需要验证版本号
//                                PackageInfo info = context.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
//                                if (info.versionCode == versionCodeServer) {//是最新文件，可能下载了一半
//                                    downloadLength = file.length();
//                                } else {//不是最新文件，重新下载
//                                    file.delete();
//                                    downloadLength = 0;
//                                }
//                            } else {
//                                downloadLength = file.length();
//                            }
                            downloadLength = file.length();
                        }
                        System.out.println("本地文件长度：" + downloadLength);
                        if (downloadLength > contentLength) {
                            //异常，删除文件重新下
                            file.delete();
                            downloadLength = 0;
                        } else if (downloadLength == contentLength) {
                            //下载已经完成
                            return Observable.empty();
                        }
                        return NetWork.Companion.getInstance().down("bytes=" + downloadLength + "-" + contentLength, url);
                    }
                })
                .map(new Function<ResponseBody, BufferedSource>() {
                    @Override
                    public BufferedSource apply(ResponseBody responseBody) {
                        totalLength = downloadLength + responseBody.contentLength();
                        System.out.println("获取文件流，总长度：" + downloadLength + "+" + responseBody.contentLength() + "=" + totalLength);
                        return responseBody.source();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<BufferedSource>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("UpdateManager.onSubscribe");
                        disposableDownload = d;
                    }

                    @Override
                    public void onNext(BufferedSource bufferedSource) {
                        System.out.println("UpdateManager.onNext");
                        try {
                            System.out.println("写入文件");
                            writeFile(bufferedSource, new File(apkPath));
                        } catch (IOException e) {
                            onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("UpdateManager.onError：" + e.getMessage());
                        stop();
                        if (listener != null)
                            listener.onFail();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("UpdateManager.onComplete");
                        stop();
                        if (listener != null)
                            listener.onComplete();
                        //安装apk
//                        installApk(apkPath,context);
                    }
                });
    }

    public void stop() {
        if (disposableDownload != null)
            disposableDownload.dispose();
    }

    /**
     * 写入文件
     */
    private void writeFile(BufferedSource source, File file) throws IOException {
//        if (!file.getParentFile().exists())
//            file.getParentFile().mkdirs();

//        if (file.exists())
//            file.delete();

        BufferedSink bufferedSink = Okio.buffer(Okio.appendingSink(file));
        bufferedSink.writeAll(source);

        bufferedSink.close();
        source.close();
    }

    /**
     * 安装APK文件
     */
    public void installApk(String filePath, Context context) {
        File apkfile = new File(filePath);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(getUriForFile(context, apkfile),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.izis.yzext.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
