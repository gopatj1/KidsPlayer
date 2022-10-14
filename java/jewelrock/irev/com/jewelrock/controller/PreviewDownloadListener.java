package jewelrock.irev.com.jewelrock.controller;

import android.util.Log;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException;

import jewelrock.irev.com.jewelrock.BuildConfig;
import jewelrock.irev.com.jewelrock.model.Error;

public class PreviewDownloadListener extends FileDownloadListener {

    @Override
    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void completed(BaseDownloadTask task) {
        if (BuildConfig.DEBUG) Log.d("!!!", "Download complete:" + task.getUrl());
        DataController.INSTANCE.setPreviewLoaded(task);
    }

    @Override
    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    protected void error(BaseDownloadTask task, Throwable e) {
        if (BuildConfig.DEBUG) Log.e("!!!", "Download error:" + task.getUrl());
        if (e instanceof FileDownloadOutOfSpaceException) {
            ErrorController.registerError(Error.NO_DISK_SPACE_FOR_PREVIEW, "Недостаточно свободного места на диске для загрузки демонстрационных роликов.", false);
        }
    }

    @Override
    protected void warn(BaseDownloadTask task) {
        if (BuildConfig.DEBUG) Log.w("!!!", "Download warning:" + task.getUrl());
    }
}
