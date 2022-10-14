package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.Video;

/**
 * Created by Юрий on 02.02.2017.
 */

public class StickyHeadersVideoSavedAdapter extends VideoSavedAdapter implements StickyRecyclerHeadersAdapter<ViewHolderBase<Video>> {

    private int indexPlaylist;

    public StickyHeadersVideoSavedAdapter(List<Video> items) {
        super(items);
    }

    @Override
    public long getHeaderId(int position) {
        // set headers to songs, which have negative id
        try {
            if (getItems().get(position).getPlaylist().getId() < 0)
                return getItems().get(position).getPlaylist().getId() * -1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //separate header per playlist id
        indexPlaylist = 0;
        for (int i = 0; i < position; i++) {
            if (getItems().get(i).getId().equals(getItems().get(position).getId()))
                indexPlaylist++;
        }
        return getItems().get(position).getPlaylist(indexPlaylist).getId();
    }

    @Override
    public ViewHolderBase<Video> onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_header_saved, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolderBase<Video> holder, int position) {
        holder.init(getItems().get(position), position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    class ViewHolder extends ViewHolderBase<Video> {
        @BindView(R.id.name)
        TextView name;
        Video video;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(v1 -> {
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void init(Video item, int position) {
            video = item;
            this.position = position;

            try {
                if (item.getId() < 0) {
                    if (item.getPlaylist().getName().equals("Песни"))
                        name.setText("Песни");
                    else
                        name.setText(item.getPlaylist().getName().replace("\\n", " ") + " (Песни)");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            indexPlaylist = 0;
            for (int i = 0; i < position; i++) {
                if (getItems().get(i).getId().equals(getItems().get(position).getId()))
                    indexPlaylist++;
            }
            name.setText(item.getPlaylist(indexPlaylist).getName().replace("\\n", " "));
        }
    }
}
