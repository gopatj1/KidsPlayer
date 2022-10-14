package jewelrock.irev.com.jewelrock.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;

import static jewelrock.irev.com.jewelrock.BaseRealmActivity.isCurrentApplicationId;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;


public class PlaylistVideoAdapter extends RecyclerView.Adapter<ViewHolderBase<Video>> {
    private List<Video> mItems;
    private LayoutInflater inflater;
    private OnItemClick<Video> onItemClick;
    private int currentPosition;
    private boolean isFavorites;
    private OnItemClick<Video> onRemoveClick;
    private OnItemClick<Video> onDownloadClick;
    private boolean isPaid;
    private boolean isStreaming;

    public PlaylistVideoAdapter(List<Video> items, boolean isFavorites) {
        mItems = items;
        this.isFavorites = isFavorites;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    public PlaylistVideoAdapter setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public PlaylistVideoAdapter setStreaming(boolean streaming) {
        isStreaming = streaming;
        return this;
    }

    public List<Video> getmItems() {
        return mItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase<Video> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_video_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase<Video> holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void add(Video video) {
        mItems.add(0, video);
        notifyItemInserted(1);
    }

    public void setOnItemClick(OnItemClick<Video> callback) {
        onItemClick = callback;
    }

    public void setOnRemoveClick(OnItemClick<Video> onRemoveClick) {
        this.onRemoveClick = onRemoveClick;
    }

    public PlaylistVideoAdapter setOnDownloadClick(OnItemClick<Video> onDownloadClick) {
        this.onDownloadClick = onDownloadClick;
        return this;
    }

    public void setNext() {
        setCurrentPosition(currentPosition + 1);
    }

    public void setPrev() {
        setCurrentPosition(currentPosition - 1);
    }

    private void setCurrentPosition(int position) {
        if (position < 0) position = 0;
        if (position - 1 >= mItems.size()) return;
        Video episode = mItems.get(position);
        if (onItemClick != null) onItemClick.onClick(episode, position);
        currentPosition = position;
        notifyDataSetChanged();
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.name)
        TextView nameTextView;
        @BindView(R.id.pictureImageView)
        ImageView picture;

        @BindView(R.id.card_tap_area)
        FrameLayout root;
        @BindView(R.id.timeText)
        TextView timeTextView;
        @BindView(R.id.btn_download)
        ImageButton downloadBtn;
        @BindView(R.id.btn_rem)
        ImageButton removeBtn;
        @BindView(R.id.circleView)
        CircleProgressView circleProgressView;
        @BindView(R.id.like)
        ImageView like;
        @BindView(R.id.informer_eye)
        ImageView preview;
        @BindView(R.id.shop)
        ImageView shop;

        int position;
        Video video;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            root.setOnClickListener(v1 -> setCurrentPosition(position));
            downloadBtn.setOnClickListener(view -> {
                Log.i("PlayList", "Download Video: " + video.getVideoSource());
                if (onDownloadClick != null) onDownloadClick.onClick(video, position);

            });
            removeBtn.setOnClickListener(view -> {
                if (onRemoveClick != null) onRemoveClick.onClick(video, position);
            });
        }

        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;

            GlideApp.with(picture.getContext())
                    .load(item.getImage())
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(picture);
            nameTextView.setText(item.getName().replace("\\n", " "));

            removeBtn.setVisibility(isFavorites && !isStreaming ? View.VISIBLE : View.GONE);

            int progress = video.getVideoLoadingProgress();
            boolean isLoading = progress >= 0 && progress < 100;
            circleProgressView.setVisibility(isLoading && (isPaid || video.isFree()) && !isStreaming ? View.VISIBLE : View.GONE);
            if (isLoading) {
                if (progress > 0) {
                    circleProgressView.setValueAnimated(progress < 25 ? 25 : progress);
                } else circleProgressView.spin();
                // при скачивании мульта, кнопка загрузки заменяется на более маленькую, совместимую с крутящимся прогресс-баром
                // в les она не заменяется и остается такой же по размерам
                if (!isCurrentApplicationId(LES)) downloadBtn.setImageResource(R.drawable.btn_download_small);
            } else {
                downloadBtn.setImageResource(R.drawable.btn_download_small_default);
            }

            downloadBtn.setVisibility((isPaid || video.isFree())&& progress < 100 && !isStreaming ? View.VISIBLE : View.GONE);
            if (isPaid || video.isFree() || isStreaming)
                timeTextView.setText(item.getVideoSourceDuration() != null
                        ? item.getVideoSourceDuration() : item.getVideoCompressedDuration());
            else
                timeTextView.setText(item.getVideoPreviewDuration());

            like.setVisibility(item.isFavorite() && !isFavorites && !isStreaming ? View.VISIBLE : View.GONE);
            preview.setVisibility(!item.isFavorite() && !isFavorites && !(isPaid || video.isFree()) && !isStreaming ? View.VISIBLE : View.GONE);

            if (!item.isFree() && !isPaid && item.getVideoPreview() == null && !isStreaming) {
                like.setVisibility(View.GONE);
                preview.setVisibility(View.GONE);
                shop.setVisibility(View.VISIBLE);
                timeTextView.setText(item.getVideoSourceDuration() != null
                        ? item.getVideoSourceDuration() : item.getVideoCompressedDuration());
            } else {
                shop.setVisibility(View.GONE);
            }
        }
    }
}