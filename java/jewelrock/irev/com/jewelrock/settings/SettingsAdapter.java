package jewelrock.irev.com.jewelrock.settings;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.SettingsItem;
import jewelrock.irev.com.jewelrock.ui.adapters.ViewHolderBase;

public class SettingsAdapter extends RecyclerView.Adapter<ViewHolderBase> {
    private ArrayList<SettingsItem> mItems;
    private LayoutInflater inflater;

    private View.OnClickListener itemClickListener;

    public SettingsAdapter(ArrayList<SettingsItem> menu, View.OnClickListener itemClickListener) {
        mItems = menu;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(inflater.inflate(R.layout.item_settings_layout, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }
    public class ViewHolder extends ViewHolderBase<SettingsItem> {
        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.switchView)
        SwitchCompat switchView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(itemClickListener);
        }
        @Override
        public void init(SettingsItem item, int position) {
            itemView.setTag(item);
            title.setText(item.title);
            if (item.editable) {
                switchView.setVisibility(View.VISIBLE);
            } else {
                switchView.setVisibility(View.GONE);
                title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            switchView.setChecked(item.isOn);
        }

    }
}
