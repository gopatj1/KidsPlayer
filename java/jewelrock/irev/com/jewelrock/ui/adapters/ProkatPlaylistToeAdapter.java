package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.controller.UserSettingsController;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;

public class ProkatPlaylistToeAdapter extends RecyclerView.Adapter<ViewHolderBase> {
    private List<Playlist> mItems;
    private LayoutInflater inflater;
    private Context context;
    private OnItemClick<Playlist> onItemClick;
    private OnItemClick<Playlist> onBuyClick;
    private OnItemClick<Playlist> onDownloadClick;
    private OnItemClick<Playlist> onStopDownloadClick;

    public ProkatPlaylistToeAdapter(List<Playlist> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
        context = recyclerView.getContext();
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = inflater.inflate(R.layout.item_prokat_toe_card, parent, false);
        return new ProkatPlaylistToeAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return  mItems.size();
    }

    public Video getItemByPurchase(String purchase) {
        for (Playlist p: mItems)
            if (p.getPurchase().equals(purchase))
                return  p.getVideos().first();
        return null;
    }

    public ProkatPlaylistToeAdapter setBuyVideos() {
        for (Playlist p: mItems)
            DataController.INSTANCE.setPurchase(Objects.requireNonNull(p.getVideos().first()), true);
        return this;
    }

    public void add(Playlist video) {
        mItems.add(0, video);
        notifyItemInserted(1);
    }

    public void setOnItemClick(OnItemClick<Playlist> callback) {
        onItemClick = callback;
    }

    public ProkatPlaylistToeAdapter setOnBuyClick(OnItemClick<Playlist> onBuyClick) {
        this.onBuyClick = onBuyClick;
        return this;
    }

    public ProkatPlaylistToeAdapter setOnDownloadClick(OnItemClick<Playlist> onDownloadClick) {
        this.onDownloadClick = onDownloadClick;
        return this;
    }

    public ProkatPlaylistToeAdapter setOnStopDownloadClick(OnItemClick<Playlist> onStopDownloadClick) {
        this.onStopDownloadClick = onStopDownloadClick;
        return this;
    }

    private void setCurrentPosition(int position) {
        if (position < 0) position = 0;
        if (position - 1 >= mItems.size()) return;
        Playlist episode = mItems.get(position);
        if (onItemClick != null) onItemClick.onClick(episode, position);
        int currentPosition = position;
        notifyDataSetChanged();
    }

    class ViewHolder extends ViewHolderBase<Playlist> {
        @BindView(R.id.item_name)
        TextView name_text;
        @BindView(R.id.item_main_image)
        ImageView main_picture;
        @BindView(R.id.buy_btn)
        ImageButton buyBtn;
        @BindView(R.id.download_btn)
        ImageButton downloadBtn;
        @BindView(R.id.downloaded_text)
        ImageView downloadedText;
        @BindView(R.id.downloading_progress_frameLay)
        FrameLayout downloadingProgressFrameLay;
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;
        @BindView(R.id.progress_text)
        TextView progressText;
        @BindView(R.id.progress_text_shadow)
        TextView progressTextShadow;
        Playlist playlist;
        @BindView(R.id.card_tap_area)
        FrameLayout tapArea;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            tapArea.setOnClickListener(v1 -> setCurrentPosition(position));

            downloadBtn.setOnClickListener(view -> {
                Log.i("PlayList", "Download Video: " + Objects.requireNonNull(playlist.getVideos().first()).getVideoSource());
                if (onDownloadClick != null) onDownloadClick.onClick(playlist, position);

            });
            downloadingProgressFrameLay.setOnClickListener(view -> {
                Log.i("PlayList", "Stop Downloading Video: " + Objects.requireNonNull(playlist.getVideos().first()).getVideoSource());
                if (onStopDownloadClick != null) onStopDownloadClick.onClick(playlist, position);

            });
            buyBtn.setOnClickListener(view -> {
                Log.i("PlayList", "Buy Video: " + Objects.requireNonNull(playlist.getVideos().first()).getPurchase() + " , price = "
                        + Objects.requireNonNull(playlist.getVideos().first()).getPrice().toString());
                if (onBuyClick != null) onBuyClick.onClick(playlist, position);
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void init(Playlist item, int position) {
            playlist = item;
            this.position = position;

            GlideApp.with(main_picture.getContext())
                    .load(item.getImage())
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    .into(main_picture);
            GlideApp.with(buyBtn.getContext())
                    .load(item.getImagePrice())
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    .into(buyBtn);

            if (item.getFontColor() != null) name_text.setTextColor(Color.parseColor(item.getFontColor()));
            name_text.setText(item.getName().replace("\\n", "\n"));

            int progress = Objects.requireNonNull(item.getVideos().get(0)).getVideoLoadingProgress();
            boolean isLoaded = progress == 100;
            boolean isLoading = progress >= 0 && progress < 100;


            if (Objects.requireNonNull(item.getVideos().get(0)).getGoogleLink() != null
                    && Objects.requireNonNull(item.getVideos().get(0)).getGoogleLink().contains("play.google.com")) {
                buyBtn.setVisibility(View.GONE);
                downloadBtn.setVisibility(View.GONE);
                downloadingProgressFrameLay.setVisibility(View.GONE);
                downloadedText.setVisibility(View.VISIBLE);
                downloadedText.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.prokat_btn_play_market));
                return;
            } else {
                downloadedText.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.prokat_text_downloaded));
            }
            if (!Objects.requireNonNull(item.getVideos().get(0)).isPurchase() && !(Objects.requireNonNull(item.getVideos().get(0)).getVideoOfCurrentApplication()
                    && UserSettingsController.loadUserSettings(Realm.getDefaultInstance()).isPaid())) {
                buyBtn.setVisibility(!isLoaded ? View.VISIBLE: View.GONE);
                downloadBtn.setVisibility(View.GONE);
                downloadingProgressFrameLay.setVisibility(View.GONE);
                downloadedText.setVisibility(isLoaded ? View.VISIBLE : View.GONE);
                return;
            }
            buyBtn.setVisibility(View.GONE);
            downloadingProgressFrameLay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                progressBar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.prokat_downloading));
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
                progressTextShadow.setText(progress + "%");
                progressTextShadow.getPaint().setStrokeWidth(4);
                progressTextShadow.getPaint().setStyle(Paint.Style.STROKE);
            }
            downloadBtn.setVisibility((isLoaded || isLoading? View.GONE : View.VISIBLE));
            downloadedText.setVisibility((isLoaded ? View.VISIBLE : View.GONE));
        }
    }
}