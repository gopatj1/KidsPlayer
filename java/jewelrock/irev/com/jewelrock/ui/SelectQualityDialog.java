package jewelrock.irev.com.jewelrock.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.BaseRealmActivity;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;

import static jewelrock.irev.com.jewelrock.BaseRealmActivity.isCurrentApplicationId;
import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;

public class SelectQualityDialog extends AlertDialog {
    private Video video;
    private Playlist playlist;
    private int quality = 0;
    private ISelectQuality selectQuality;

    public SelectQualityDialog(@NonNull Context context, Video video, ISelectQuality selectQuality) {
        super(context);
        this.video = video;
        this.selectQuality = selectQuality;
        init();
    }

    public SelectQualityDialog(@NonNull Context context, Playlist playlist, ISelectQuality selectQuality) {
        super(context);
        this.playlist = playlist;
        this.selectQuality = selectQuality;
        init();
    }

    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.checkboxLQ)
    AppCompatRadioButton radioButtonLQ;
    @BindView(R.id.checkboxHQ)
    AppCompatRadioButton radioButtonHQ;

    @SuppressLint("DefaultLocale")
    private void init() {
        setTitle(playlist == null
                ? isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES)
                    ? String.format("?????????????? ?????????? \"%s\"?", video.getName().replace("\\n", " "))
                    : String.format("?????????????? ???????????????????? \"%s\"?", video.getName().replace("\\n", " "))
                : String.format("?????????????? ?????????? \"%s\"?", playlist.getName().replace("\\n", " ")));

        long totalSizeHQ = 0;
        long totalSizeLQ = 0;
        boolean allHasVideoSource = true;
        boolean allHasCompressedVideoSource = true;
        int totalTime = 0;
        List<Video> videos = new ArrayList<>();
        if (playlist != null){
            videos.addAll(playlist.getVideos());
        } else {
            videos.add(video);
        }
        for (Video v : videos) {
            if (v.getVideoSourceSize() != null) totalSizeHQ += v.getVideoSourceSize();
            if (v.getVideoCompressedSize() != null) totalSizeLQ += v.getVideoCompressedSize();
            totalTime += v.getVideoSourceDurationSec() != null ? v.getVideoSourceDurationSec() : v.getVideoCompressedDurationSec();
            if (v.getVideoSource() == null) allHasVideoSource = false;
            if (v.getVideoCompressed() == null) allHasCompressedVideoSource = false;
        }

        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_quality, null);
        ButterKnife.bind(this, dialogView);
        if (totalSizeLQ != 0 && allHasCompressedVideoSource){
            radioButtonLQ.setText(String.format("?????????????? ???????????????? (%.2f ????)", (float) totalSizeLQ / 1024 / 1024));
        } else {
            radioButtonLQ.setVisibility(View.GONE);
            radioButtonHQ.setChecked(true);
            quality = 1;
        }
        if (totalSizeHQ != 0 && allHasVideoSource){
            radioButtonHQ.setText(String.format("?????????????? ???????????????? Full HD (%.2f ????)", (float) totalSizeHQ / 1024 / 1024));
        } else {
            radioButtonHQ.setVisibility(View.GONE);
            radioButtonLQ.setChecked(true);
            quality = 0;
        }

        String text;
        if (playlist != null) {
            text = isCurrentApplicationId(ERALASH) || isCurrentApplicationId(LES)
                ? String.format("???????????????????? ?????????????? - %d\n" + "?????????? ???????????????????????? - %s", videos.size(), getTime(totalTime))
                : String.format("???????????????????? ???????????????????????? - %d\n" + "?????????? ???????????????????????? - %s", videos.size(), getTime(totalTime));
        } else{
            text = String.format("???????????????????????? - %s",
                    getTime(totalTime));
        }

        this.text.setText(text);

        setView(dialogView);

        setOnDismissListener(dialogInterface -> {
            if (selectQuality != null) selectQuality.onSelect(quality);
        });
        setCancelable(true);

        setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            BaseRealmActivity ownerActivity = (BaseRealmActivity) getOwnerActivity();
            if (ownerActivity != null) {
                for (Video v: videos) ownerActivity.analyticsLogEvent("??????????????????????", "????????????????????",
                        v.getName().replace("\\", "").replace("n", " "));
            }
            DataController.INSTANCE.startDownloadingVideos(videos, quality);
        });
        setButton(DialogInterface.BUTTON_NEGATIVE,"????????????", (dialogInterface, i) -> quality = -1);
        setCancelable(false);
    }

    @OnClick(R.id.checkboxLQ)
    void OnLQClick() {
        quality = 0;
    }

    @OnClick(R.id.checkboxHQ)
    void OnHQClick() {
        quality = 1;
    }

    @SuppressLint("DefaultLocale")
    private String getTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        if (hours < 1) return String.format("%02d:%02d", minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public interface ISelectQuality {
        void onSelect(int quality);
    }
}
