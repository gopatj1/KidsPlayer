package jewelrock.irev.com.jewelrock.ui.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.BaseFabActivity;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Playlist;
import jewelrock.irev.com.jewelrock.model.Video;

import static jewelrock.irev.com.jewelrock.BaseRealmActivity.isCurrentApplicationId;
import static jewelrock.irev.com.jewelrock.utils.Constants.ERALASH;
import static jewelrock.irev.com.jewelrock.utils.Constants.LES;


public class PlaylistAdapter extends RecyclerView.Adapter<ViewHolderBase> {
    private List<Playlist> mItems;
    private LayoutInflater inflater;
    private OnItemClick<Playlist> onItemClick;
    // for 1 playlist item card ///////////////////////////////////////////////////////////////////////
    private OnItemClick<Playlist> onDownloadClick;
    private boolean isPaid;
    private boolean isStreaming = false;
    private boolean isFavorites = false;
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public PlaylistAdapter(List<Playlist> items) {
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
        View v;
        v = inflater.inflate(R.layout.item_playlist_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return  mItems.size();
    }

    public void add(Playlist video) {
        mItems.add(0, video);
        notifyItemInserted(1);
    }

    public void setOnItemClick(OnItemClick<Playlist> callback) {
        onItemClick = callback;
    }

    // for 1 playlist item card ///////////////////////////////////////////////////////////////////////
    public PlaylistAdapter setPaid(boolean paid) {
        isPaid = paid;
        return this;
    }

    public PlaylistAdapter setOnDownloadClick(OnItemClick<Playlist> onDownloadClick) {
        this.onDownloadClick = onDownloadClick;
        return this;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////



    private void setCurrentPosition(int position) {
        if (position < 0) position = 0;
        if (position - 1 >= mItems.size()) return;
        Playlist episode = mItems.get(position);
        if (onItemClick != null) onItemClick.onClick(episode, position);
        notifyDataSetChanged();
    }

    class ViewHolder extends ViewHolderBase<Playlist> {
        @BindView(R.id.name)
        TextView text;
        @BindView(R.id.mult_counts_text_in_playlist)
        TextView multCountsText;
        @BindView(R.id.pictureImageView)
        ImageView picture;
        @BindView(R.id.imFrame)
        ImageView imFrame;
        Playlist playlist;
        @BindView(R.id.card_tap_area)
        FrameLayout tapArea;
        int position;

        // for 1 playlist item card ///////////////////////////////////////////////////////////////////
        @BindView(R.id.name_1_video)
        TextView nameTextView1video;
        @BindView(R.id.pictureImageView_1_video)
        ImageView picture1video;
        @BindView(R.id.card_tap_area_1_video_main)
        FrameLayout rootMain;
        @BindView(R.id.card_tap_area_1_video)
        FrameLayout root;
        @BindView(R.id.timeText)
        TextView timeTextView;
        @BindView(R.id.btn_download)
        ImageButton downloadBtn;
        @BindView(R.id.circleView_1_video)
        CircleProgressView circleProgressView1video;
        @BindView(R.id.like)
        ImageView like;
        @BindView(R.id.informer_eye)
        ImageView preview;
        @BindView(R.id.shop)
        ImageView shop;
        ////////////////////////////////////////////////////////////////////////////////////////////

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            tapArea.setOnClickListener(v1 -> setCurrentPosition(position));

            // for 1 playlist item card ///////////////////////////////////////////////////////////////
            root.setOnClickListener(v1 -> setCurrentPosition(position));
            downloadBtn.setOnClickListener(view -> {
                Log.i("PlayList", "Download Video: " + Objects.requireNonNull(playlist.getVideos().get(0)).getVideoSource());
                if (onDownloadClick != null) onDownloadClick.onClick(playlist, position);

            });
            ////////////////////////////////////////////////////////////////////////////////////////
        }

        @Override
        public void init(Playlist item, int position) {
            playlist = item;
            this.position = position;

            // for 1 playlist item card ///////////////////////////////////////////////////////////////
            Video firstVideoOfPlaylist = item.getVideos().get(0); // first video of playlist which contain 1 video
            if (item.getVideos().size() == 1 && item.getPlaylistVideo() != null) {
                tapArea.setVisibility(View.GONE); // hide playlist square icon
                rootMain.setVisibility(View.VISIBLE); // show video square icon

                GlideApp.with(picture1video.getContext())
                        .load(Objects.requireNonNull(firstVideoOfPlaylist).getImage())
                        .fallback(R.drawable.im_no_image)
                        .placeholder(R.drawable.im_no_image)
                        .into(picture1video);

                nameTextView1video.setText(firstVideoOfPlaylist.getName());

                int progress = firstVideoOfPlaylist.getVideoLoadingProgress();
                boolean isLoading = progress >= 0 && progress < 100;
                circleProgressView1video.setVisibility(isLoading && (isPaid || firstVideoOfPlaylist.isFree())
                        && !isStreaming ? View.VISIBLE : View.GONE);
                if (isLoading) {
                    if (progress > 0) {
                        circleProgressView1video.setValueAnimated(progress < 25 ? 25 : progress);
                    } else circleProgressView1video.spin();
                    // при скачивании мульта, кнопка загрузки заменяется на более маленькую, совместимую с крутящимся прогресс-баром
                    // в les она не заменяется и остается такой же по размерам
                    if (!isCurrentApplicationId(LES)) downloadBtn.setImageResource(R.drawable.btn_download_small);
                } else {
                    downloadBtn.setImageResource(R.drawable.btn_download_small_default);
                }

                downloadBtn.setVisibility((isPaid || firstVideoOfPlaylist.isFree())&& progress < 100
                        && !isStreaming ? View.VISIBLE : View.GONE);
                if (isPaid || firstVideoOfPlaylist.isFree() || isStreaming)
                    timeTextView.setText(firstVideoOfPlaylist.getVideoSourceDuration() != null
                            ? firstVideoOfPlaylist.getVideoSourceDuration() : firstVideoOfPlaylist.getVideoCompressedDuration());
                else
                    timeTextView.setText(firstVideoOfPlaylist.getVideoPreviewDuration());

                like.setVisibility(firstVideoOfPlaylist.isFavorite() && !isFavorites && !isStreaming ? View.VISIBLE : View.GONE);
                preview.setVisibility(!firstVideoOfPlaylist.isFavorite() && !isFavorites && !(isPaid ||
                        firstVideoOfPlaylist.isFree()) && !isStreaming ? View.VISIBLE : View.GONE);

                if (!firstVideoOfPlaylist.isFree() && !isPaid && firstVideoOfPlaylist.getVideoPreview() == null && !isStreaming) {
                    like.setVisibility(View.GONE);
                    preview.setVisibility(View.GONE);
                    shop.setVisibility(View.VISIBLE);
                    timeTextView.setText(firstVideoOfPlaylist.getVideoSourceDuration() != null
                            ? firstVideoOfPlaylist.getVideoSourceDuration() : firstVideoOfPlaylist.getVideoCompressedDuration());
                } else {
                    shop.setVisibility(View.GONE);
                }
             ////////////////////////////////////////////////////////////////////////////////////////
            } else {
                tapArea.setVisibility(View.VISIBLE); // show playlist square icon
                rootMain.setVisibility(View.GONE); // hide video square icon
                GlideApp.with(picture.getContext())
                        .load(item.getImage())
                        .fallback(R.drawable.im_no_image)
                        .placeholder(R.drawable.im_no_image)
                        .into(picture);
                text.setText(item.getName().replace("\\n", "\n"));
                // в eralash название каждого плейлиста имеет свой определенный цвет, заимстовованный с сервера, у всех остальных цвет стандартный
                if (item.getFontColor() != null && isCurrentApplicationId(ERALASH))
                    text.setTextColor(Color.parseColor(item.getFontColor()));
                multCountsText.setText(String.valueOf(item.getVideos().size()));
                if (item.getVideos().size() > 9) {
                    ((FrameLayout.LayoutParams) multCountsText.getLayoutParams()).leftMargin =
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, BaseFabActivity.displayMetrics);
                } else {
                    ((FrameLayout.LayoutParams) multCountsText.getLayoutParams()).leftMargin =
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 11, BaseFabActivity.displayMetrics);
                }
            }
        }
    }
}