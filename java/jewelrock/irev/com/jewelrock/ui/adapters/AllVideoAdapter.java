package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.navigators.AllVideoActions;


public class AllVideoAdapter extends RecyclerView.Adapter<ViewHolderBase<Video>> {
    private List<Video> mItems;
    private LayoutInflater inflater;
    private boolean isPaid;
    private boolean isPopular;
    private boolean isStreaming;

    private AllVideoActions actions;

    public AllVideoAdapter(List<Video> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    public AllVideoAdapter setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public AllVideoAdapter setPopularStatus(boolean popular) {
        isPopular = popular;
        return this;
    }

    public AllVideoAdapter setStreaming(boolean streaming) {
        isStreaming = streaming;
        return this;
    }

    public AllVideoAdapter setActions(AllVideoActions actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase<Video> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_all_videos, parent, false);
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

    public List<Video> getItems() {
        return mItems;
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.number)
        TextView number;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.popular_rating)
        TextView popularRating;
        @BindView(R.id.liked)
        AppCompatImageView liked;
        @BindView(R.id.not_liked)
        AppCompatImageView notLiked;
        @BindView(R.id.favorite)
        FrameLayout favouriteFrameLay;

        @BindView(R.id.trashbin)
        AppCompatImageView trashbin;
        @BindView(R.id.download)
        AppCompatImageView download;
        @BindView(R.id.buy)
        AppCompatImageView buy;

        @BindView(R.id.circleView)
        CircleProgressView circleProgressView;

        Video video;
        int position;

        @OnClick(R.id.favorite)
        void onFavoriteClick(View v) {
            if (isPaid || video.isFree()) actions.changeLikeStatusOfVideo(video);
        }

        @OnClick(R.id.root)
        void onRootClick(View v) {
            if (!video.isFree() && !isPaid && video.getVideoPreview() == null) {
                actions.buyVideo(video);
                return;
            }
            actions.openVideo(video);
        }

        @OnClick(R.id.download)
        void onDownloadClick(View v) {
            actions.downloadVideo(video);
        }

        @OnClick(R.id.buy)
        void onBuyClick(View v) {
            actions.buyVideo(video);
        }

        @OnClick(R.id.trashbin)
        void onTrashbinClick(View v) {
            actions.deleteVideo(video);
        }

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;
            number.setText((position+1)+ "");
            number.setTypeface(null, isPopular ? Typeface.BOLD : Typeface.NORMAL);
            name.setText(item.getName().replace("\\n", " "));
            time.setText(item.getVideoSourceDuration() != null ? item.getVideoSourceDuration() : item.getVideoCompressedDuration());
            liked.setVisibility((isPaid || video.isFree()) && video.isFavorite() && !isStreaming ? View.VISIBLE : View.GONE);
            notLiked.setVisibility((isPaid || video.isFree()) && !video.isFavorite() && !isStreaming ? View.VISIBLE : View.GONE);
            popularRating.setVisibility(isPopular ? View.VISIBLE : View.GONE);
            try {
                popularRating.setText(item.getVideoLikes().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!isPopular) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) name.getLayoutParams();
                lp.weight = 58;
                name.setLayoutParams(lp);
            }

            int progress = video.getVideoLoadingProgress();
            boolean isLoaded = progress == 100;
            boolean isLoading = progress >= 0 && progress < 100;

            if (isStreaming) {
                buy.setVisibility(View.GONE);
                download.setVisibility(View.GONE);
                circleProgressView.setVisibility(View.GONE);
                trashbin.setVisibility(View.GONE);
                return;
            }
            if (!(isPaid || video.isFree())){
                buy.setVisibility(!isLoaded ? View.VISIBLE: View.GONE);
                download.setVisibility(View.GONE);
                circleProgressView.setVisibility(View.GONE);
                trashbin.setVisibility(isLoaded ? View.VISIBLE : View.GONE);
                return;
            }
            buy.setVisibility(View.GONE);
            circleProgressView.setVisibility(isLoading  ? View.VISIBLE : View.GONE);
            if (isLoading) {
                if (progress > 0) {
                    circleProgressView.setValueAnimated(progress < 25 ? 25 : progress);
                } else circleProgressView.spin();
            }
            download.setVisibility(!isLoaded ? View.VISIBLE : View.GONE);
            trashbin.setVisibility(isLoaded ? View.VISIBLE : View.GONE);
        }
    }
}