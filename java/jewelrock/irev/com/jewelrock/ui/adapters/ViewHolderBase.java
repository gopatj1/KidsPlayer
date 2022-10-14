package jewelrock.irev.com.jewelrock.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Иоанн on 09.06.2016.
 */

public abstract class ViewHolderBase<T extends Object> extends RecyclerView.ViewHolder {
    public ViewHolderBase(View itemView) {
        super(itemView);
    }

    public abstract void init(T item, int position);
}
