package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;
import jewelrock.irev.com.jewelrock.navigators.AllPlaylistActions;

public class AllPlaylistsAdapter extends RecyclerView.Adapter<ViewHolderBase<Playlist>> {
    private List<Playlist> mItems;
    private LayoutInflater inflater;
    private boolean isPaid;

    private AllPlaylistActions actions;

    public AllPlaylistsAdapter(List<Playlist> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    public AllPlaylistsAdapter setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public AllPlaylistsAdapter setActions(AllPlaylistActions actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase<Playlist> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_all_videos, parent, false);
        return new AllPlaylistsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase<Playlist> holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<Playlist> getItems() {
        return mItems;
    }

    class ViewHolder extends ViewHolderBase<Playlist> {
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

        @BindView(R.id.trashbin)
        AppCompatImageView trashbin;
        @BindView(R.id.download)
        AppCompatImageView download;
        @BindView(R.id.buy)
        AppCompatImageView buy;

        @BindView(R.id.circleView)
        CircleProgressView circleProgressView;

        Playlist playlist;
        int position;

        @OnClick(R.id.root)
        void onRootClick() {
            if (!isPaid) {
                actions.buyPlaylist(playlist);
                return;
            }
            actions.openPlaylist(playlist);
        }

        @OnClick(R.id.download)
        void onDownloadClick() {
            actions.downloadPlaylist(playlist);
        }

        @OnClick(R.id.buy)
        void onBuyClick() {
            actions.buyPlaylist(playlist);
        }

        @OnClick(R.id.trashbin)
        void onTrashbinClick() {
            actions.deletePlaylist(playlist);
        }

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void init(Playlist item, int position) {
            playlist = item;
            this.position = position;
            number.setText((position+1)+ "");
            name.setText(item.getName().replace("\\n", " "));
            time.setText(String.valueOf(playlist.getVideos().size()));
            liked.setVisibility(View.GONE);
            notLiked.setVisibility(View.GONE);
            popularRating.setVisibility(View.GONE);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) name.getLayoutParams();
            lp.weight = 62;
            name.setLayoutParams(lp);

            int progress = 0;
            boolean isAllLoading = true;
            for (Video v : playlist.getVideos()) {
                progress += v.getVideoLoadingProgress();
                if (v.getVideoLoadingProgress() < 0)
                    isAllLoading = false;
            }
            progress /= playlist.getVideos().size();
            boolean isAllLoaded = progress == 100;
            if (isAllLoaded) isAllLoading = false;

            if (!isPaid){
                buy.setVisibility(!isAllLoaded ? View.VISIBLE: View.GONE);
                download.setVisibility(View.GONE);
                circleProgressView.setVisibility(View.GONE);
                trashbin.setVisibility(isAllLoaded ? View.VISIBLE : View.GONE);
                return;
            }
            buy.setVisibility(View.GONE);
            circleProgressView.setVisibility(isAllLoading ? View.VISIBLE : View.GONE);
            if (isAllLoading) {
                if (progress > 0) {
                    circleProgressView.setValueAnimated(progress < 25 ? 25 : progress);
                } else circleProgressView.spin();
            }
            download.setVisibility(!isAllLoaded ? View.VISIBLE : View.GONE);
            trashbin.setVisibility(isAllLoaded ? View.VISIBLE : View.GONE);
        }
    }
}