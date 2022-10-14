package jewelrock.irev.com.jewelrock.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import java.util.Collection;
import java.util.Objects;

import jewelrock.irev.com.jewelrock.BaseRealmActivity;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.ui.SelectQualityDialog;
import kotlin.jvm.JvmOverloads;

/**
 * Created by Юрий on 27.01.2017.
 */

public class DialogUtils {

    public static void showDownloadDialog(Activity activity, Video video) {
        showDownloadDialog(activity, video, null);
    }

    public static void showDownloadDialog(Activity activity, Playlist playlist) {
        showDownloadDialog(activity, playlist, null);
    }

    public static void showDownloadDialog(Activity activity, Playlist playlist, SelectQualityDialog.ISelectQuality selectQuality) {
        SelectQualityDialog dialog = new SelectQualityDialog(activity, playlist, selectQuality);
        showImmersiveDialog(dialog);
    }

    public static AlertDialog showDownloadDialog(Activity activity, Video video, SelectQualityDialog.ISelectQuality selectQuality) {
        SelectQualityDialog dialog = new SelectQualityDialog(activity, video, selectQuality);
        showImmersiveDialog(dialog);
        return dialog;
    }

    public static void showCancelLoadingDialog(Activity activity, final Video video, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Хотите остановить скачивание?")
                .setPositiveButton("OK", (dialogInterface, i) -> DataController.INSTANCE.cancelDownloadingVideo(video))
                .setNegativeButton("Отмена", (dialogInterface, i) -> {
                })
                .setOnDismissListener(listener);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    @JvmOverloads
    public static void showCancelLoadingDialog(Activity activity, final Collection<? extends Video> videos, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Хотите остановить скачивание всех мультов данного раздела?")
                .setPositiveButton("OK", (dialogInterface, i) -> DataController.INSTANCE.cancelDownloadingVideos(videos))
                .setNegativeButton("Отмена", (dialogInterface, i) -> {
                })
                .setOnDismissListener(listener);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }


    public static void showImmersiveDialog(AlertDialog dialog) {
        Objects.requireNonNull(dialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        //Set the dialog to immersive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        //Clear the not focusable flag from the window
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public static void showDislikeDialog(BaseRealmActivity activity, Video video, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Хотите удалить видео из Любимых?")
                .setPositiveButton("OK", (dialogInterface, i) -> DataController.INSTANCE.setFavorite(activity, video, false))
                .setNegativeButton("Отмена", (dialogInterface, i) -> {
                })
                .setOnDismissListener(listener);
        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void showDeleteDialog(BaseRealmActivity activity, Collection<Video> videos, View.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Хотите удалить " + videos.size() + " видео?")
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    for (Video v : videos)
                        activity.analyticsLogEvent("Мультфильмы", "Удаление",
                                v.getName().replace("\\", "").replace("n", " "));
                    DataController.INSTANCE.cancelDownloadingVideos(videos);
                    DataController.INSTANCE.removeDownloadedVideos(videos);
                    listener.onClick(null);
                })
                .setNegativeButton("Отмена", (dialogInterface, i) -> {
                });

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void showNoInternetDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Не удалось получить данные приложения, отсутствует интернет-соединение.")
                .setPositiveButton("Попробовать снова", (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton("Выйти", (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void showNoInternetInShopDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Не удалось восстановить покупки! Проверьте интернет-соединение.")
                .setPositiveButton("Попробовать снова", (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton("Выйти", (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void showCantLoadVideoDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Не удалось воспроизвести видео, отсутствует интернет-соединение.")
                .setPositiveButton("Попробовать снова", (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton("ОК", (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void showRewardAdNotReadyDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Внимание!")
                .setMessage("Реклама с вознаграждением еще не загрузилась! Немного подождите и попробуйте снова!")
                .setPositiveButton("Попробовать снова", (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton("Закрыть", (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void alert(Activity activity, String message) {
        alert(activity, message, null);
    }

    public static void alert(Activity activity, String message, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder bld = new AlertDialog.Builder(activity);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        AlertDialog alert = bld.create();
        if (listener != null) alert.setOnDismissListener(listener);
        showImmersiveDialog(alert);
    }

    public interface OnDialogClose {
        void onClose(int buttonId);
    }
}
