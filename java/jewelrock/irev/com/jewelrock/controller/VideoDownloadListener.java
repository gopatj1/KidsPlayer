package jewelrock.irev.com.jewelrock.controller;

import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException;

import java.io.IOException;

import jewelrock.irev.com.jewelrock.BuildConfig;
import jewelrock.irev.com.jewelrock.model.Error;

public class VideoDownloadListener extends FileDownloadListener {

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        if (BuildConfig.DEBUG) Log.d("!!!", "Download pending:" + task.getUrl() + "\n" + soFarBytes + " / " + totalBytes);
    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        long progress = (long) soFarBytes * 100 / (long) totalBytes;
        if (BuildConfig.DEBUG) Log.d("!!!", "Download progress:" + task.getUrl() + "\n" + soFarBytes + " / " + totalBytes + " = " + progress + "%");
        DataController.INSTANCE.setVideoLoadingPercent(task, (int) progress);
    }

    @Override
    protected void completed(BaseDownloadTask task) {
        if (BuildConfig.DEBUG) Log.d("!!!", "Download complete:" + task.getUrl());
        DataController.INSTANCE.setVideoLoaded(task);
    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        if (BuildConfig.DEBUG) Log.e("!!!", "Download error:" + task.getUrl());
        if (BuildConfig.DEBUG) Log.e("!!!", e.getClass().getName() + " " +e.getMessage());
        if (e instanceof FileDownloadOutOfSpaceException) {
            DataController.INSTANCE.cancelDownloadingVideo(task);
            ErrorController.registerError(Error.NO_DISK_SPACE, "Недостаточно свободного места на диске. Освободите место и попробуйте снова.", true);
        } else if (e instanceof IOException){
            ErrorController.registerError(Error.NO_INTERNET_CONNECTION, "Нет доступа к интернет. Скачивание файлов невозможно. Подключите интернет и попробуйте снова.", true);
        }
    }

    @Override
    protected void warn(BaseDownloadTask task) {
        if (BuildConfig.DEBUG) Log.w("!!!", "Download warning:" + task.getUrl());
    }
}
