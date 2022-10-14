package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;


public class VideoSavedAdapter extends RecyclerView.Adapter<ViewHolderBase<Video>> {
    private List<Video> mItems;
    private LayoutInflater inflater;
    private OnItemClick<Video> onItemClick;
    private TreeSet<Video> selectedItems;
    private boolean isSelectionState;

    VideoSavedAdapter(List<Video> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
        selectedItems = new TreeSet<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase<Video> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_video_saved, parent, false);
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

    public void setOnItemClick(OnItemClick<Video> callback) {
        onItemClick = callback;
    }

    public VideoSavedAdapter setSelectionState(boolean selectionState) {
        isSelectionState = selectionState;
        selectNone();
        return this;
    }

    public boolean isSelectionState() {
        return isSelectionState;
    }

    public void selectAll() {
        selectedItems.addAll(mItems);
        notifyDataSetChanged();
    }

    public void selectNone() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemsCount() {
        return selectedItems.size();
    }

    public TreeSet<Video> getSelectedItems() {
        return selectedItems;
    }

    public void removeSelected() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.quality)
        TextView quality;
        @BindView(R.id.size)
        TextView size;
        @BindView(R.id.checkbox_checked)
        ImageView checkBoxChecked;
        @BindView(R.id.checkbox_blank)
        ImageView checkBoxBlank;

        Video video;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(v1 -> {
                if (isSelectionState) {
                    if (selectedItems.contains(video)) selectedItems.remove(video);
                    else selectedItems.add(video);
                }
                notifyDataSetChanged();
                if (onItemClick != null) onItemClick.onClick(video, position);
            });
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;
            checkBoxBlank.setVisibility(isSelectionState && !selectedItems.contains(video) ? View.VISIBLE : View.INVISIBLE);
            checkBoxChecked.setVisibility(isSelectionState && selectedItems.contains(video) ? View.VISIBLE : View.INVISIBLE);
            name.setText(item.getName().replace("\\n", " "));
            String videoFile = item.getVideoFile();

            quality.setText(item.getQuality() == 1 ? "HD" : "SD");
            if (videoFile != null) {
                File file = new File(videoFile);
                long length = file.length();
                float sizeMb = (float) length / 1024f / 1024f;
                @SuppressLint("DefaultLocale") String format = String.format("%.2f Mb", sizeMb);
                size.setText(format);
            } else {
                size.setText(String.format("%d%%", video.getVideoLoadingProgress()));
            }
        }
    }
}