package jewelrock.irev.com.jewelrock;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ParentalControlsFragment extends Fragment {

    @OnClick(R.id.close)
    void onClose(View v) {
        ((BaseFabActivity) Objects.requireNonNull(getActivity())).playSound(R.raw.tap);
        getActivity().onBackPressed();
    }

    @BindView(R.id.questionTitle)
    TextView title;

    @OnClick({R.id.choice0, R.id.choice1, R.id.choice2, R.id.choice3, R.id.choice4, R.id.choice5, R.id.choice6, R.id.choice7, R.id.choice8, R.id.choice9})
    void onChoice(View v) {
        ((BaseFabActivity) Objects.requireNonNull(getActivity())).playSound(R.raw.tap);
        boolean hasAccess = v.getId() == R.id.choice8;

        if (hasAccess) {
            ((BaseFabActivity) getActivity()).showMenu();
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public static ParentalControlsFragment newInstance() {
        return new ParentalControlsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parental_controls, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }
}
