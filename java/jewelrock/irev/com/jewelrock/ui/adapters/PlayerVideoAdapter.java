package jewelrock.irev.com.jewelrock.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;


public class PlayerVideoAdapter extends RecyclerView.Adapter<ViewHolderBase> {
    private List<Video> mItems;
    private LayoutInflater inflater;
    private OnItemClick<Video> onItemClick;
    private int currentPosition;
    private boolean isPaid;
    private boolean isStreaming;
    private boolean isSong;
    private boolean isRewarding;
    private boolean isProkat;

    public PlayerVideoAdapter(List<Video> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_player_video_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
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

    public PlayerVideoAdapter setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public PlayerVideoAdapter setStreaming(boolean streaming) {
        isStreaming = streaming;
        return this;
    }

    public PlayerVideoAdapter setSong(boolean song) {
        isSong = song;
        return this;
    }

    public PlayerVideoAdapter setRewarding(boolean rewarding) {
        isRewarding = rewarding;
        return this;
    }

    public PlayerVideoAdapter setProkat(boolean prokat) {
        isProkat = prokat;
        return this;
    }

    public void setOnItemClick(OnItemClick<Video> callback) {
        onItemClick = callback;
    }

    public boolean setNext(boolean circular) {
        if (!circular && currentPosition + 1 >= mItems.size()) return false;
        setCurrentPosition(currentPosition + 1, circular);
        return true;
    }

    public boolean setPrev(boolean circular) {
        if (!circular && currentPosition - 1 < 0) return false;
        setCurrentPosition(currentPosition - 1, circular);
        return true;
    }

    public void setCurrentPosition(int position, boolean circular) {
        if (position < 0 ) position = circular ? mItems.size() - 1 : 0;
        if (position > mItems.size() - 1) position = circular ? 0 : mItems.size() - 1;
        currentPosition = position;
        Video video = getCurrentVideo();
        if (onItemClick != null) onItemClick.onClick(video, position);
        notifyDataSetChanged();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public Video getCurrentVideo() {
        if (mItems == null) return null;
        if (mItems.size() == 0) return null;
        if (currentPosition >= mItems.size()) currentPosition = 0;
        return mItems.get(currentPosition);
    }

    public boolean hasNext() {
        return currentPosition < mItems.size() - 1;
    }

    public boolean hasPrev() {
        return currentPosition > 0;
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.timeText)
        TextView time;
        @BindView(R.id.nameText)
        TextView name;
        @BindView(R.id.pictureImageView)
        ImageView picture;
        @BindView(R.id.card_tap_area)
        FrameLayout root;
        @BindView(R.id.eye)
        ImageView eye;
        @BindView(R.id.like)
        ImageView like;
        @BindView(R.id.selector)
        ImageView selector;
        @BindBool(R.bool.isBigScreen)
        boolean isBigScreen;
        Video video;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            root.setOnClickListener(v1 -> {
                setCurrentPosition(position, false);
            });
        }

        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;
            GlideApp.with(picture.getContext())
                    .load(item.getImage())
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(picture);
            time.setText(item.getVideoSourceDuration() != null
                    ? item.getVideoSourceDuration() : item.getVideoCompressedDuration());
            if (currentPosition == position) {
                selector.setVisibility(View.VISIBLE);
            } else {
                selector.setVisibility(View.GONE);
            }
            like.setVisibility((isPaid || video.isFree()) && video.isFavorite() && !isStreaming  && !isRewarding && !isProkat ? View.VISIBLE : View.GONE);
            eye.setVisibility(!(isPaid || video.isFree()) && !isStreaming  && !isRewarding && !isProkat ? View.VISIBLE : View.GONE);
            if (isBigScreen) {
                name.setText(video.getName().replace("\\n", " "));
            } else {
                name.setVisibility(View.GONE);
            }
        }
    }
}