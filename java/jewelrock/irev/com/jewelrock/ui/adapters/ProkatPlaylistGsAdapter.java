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

import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;

public class ProkatPlaylistGsAdapter extends RecyclerView.Adapter<ViewHolderBase> {
    private List<Video> mItems;
    private LayoutInflater inflater;
    private OnItemClick<Video> onItemClick;

    public ProkatPlaylistGsAdapter(List<Video> items) {
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
        v = inflater.inflate(R.layout.item_prokat_gs_card, parent, false);
        return new ProkatPlaylistGsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return  mItems.size();
    }

    public Video getItemByPos(int pos) {
        return mItems.get(pos);
    }

    public List<Video> getItems() {
        return mItems;
    }

    public void add(Video video) {
        mItems.add(0, video);
        notifyItemInserted(1);
    }

    public void setOnItemClick(OnItemClick<Video> callback) {
        onItemClick = callback;
    }

    private void setCurrentPosition(int position) {
        if (position < 0) position = 0;
        if (position - 1 >= mItems.size()) return;
        Video episode = mItems.get(position);
        if (onItemClick != null) onItemClick.onClick(episode, position);
        notifyDataSetChanged();
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.item_name)
        TextView name_text;
        @BindView(R.id.item_main_image)
        ImageView main_picture;
        Video video;
        @BindView(R.id.card_tap_area)
        FrameLayout tapArea;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            tapArea.setOnClickListener(v1 -> setCurrentPosition(position));
        }

        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;

            GlideApp.with(main_picture.getContext())
                    .load(item.getImage())
                    .fallback(R.drawable.im_no_image)
                    .placeholder(R.drawable.im_no_image)
                    .into(main_picture);

            name_text.setText(item.getName().replace("\\n", "\n"));
        }
    }
}