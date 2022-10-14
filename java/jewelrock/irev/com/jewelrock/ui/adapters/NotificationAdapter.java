package jewelrock.irev.com.jewelrock.ui.adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.controller.DataController;
import jewelrock.irev.com.jewelrock.model.Notification;
import jewelrock.irev.com.jewelrock.utils.ExpandableTextView;

public class NotificationAdapter extends RecyclerView.Adapter<ViewHolderBase<Notification>> {
    private List<Notification> mItems;
    private LayoutInflater inflater;

    public NotificationAdapter(List<Notification> items) {
        mItems = items;
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
    }

    public List<Notification> getmItems() {
        return mItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        inflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public ViewHolderBase<Notification> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolderBase<Notification> holder, int position) {
        holder.init(mItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends ViewHolderBase<Notification> {
        @BindView(R.id.tap_area)
        LinearLayout tapArea;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.text)
        ExpandableTextView text;
        @BindView(R.id.number)
        TextView number;
        @BindView(R.id.time)
        TextView time;
        @BindView(R.id.collaps_text)
        TextView collapsText;
        Notification notification;
        int position;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            tapArea.setOnClickListener(v1 -> text.callOnClick(collapsText));
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        public void init(Notification item, int position) {
            notification = item;
            this.position = position;

            name.setText(item.getTitleName());
            text.setText(item.getText());
            time.setText(new SimpleDateFormat("dd.MM.yyyy").format(item.getTime()));
            name.setText(item.getTitleName());
            number.setText(String.valueOf(Objects.requireNonNull(DataController.INSTANCE.getNotifications()).size() - position));
        }
    }
}