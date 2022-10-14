package jewelrock.irev.com.jewelrock.ui.welcome;

/**
 * Created by makarkin on 10.07.2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jewelrock.irev.com.jewelrock.GlideApp;
import jewelrock.irev.com.jewelrock.R;
import jewelrock.irev.com.jewelrock.model.WelcomeScreen;


/**
 * A placeholder fragment containing a simple view.
 */
public class WelcomeFragment extends Fragment {
    private static final String ARG_SECTION_ID = "section_id";
    private static final String ARG_SECTION_URL = "url";
    private static String ARG_SECTION_IMAGE = "section_image";
    private static String ARG_SECTION_COLOR = "section_Color";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private OnFragmentInteractionListener mListener;
    private static final String ARG_SECTION_NAME = "section_number";
    private String text = "";
    private String img;
    private String background;
    private String url;
    private int id;

    public WelcomeFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WelcomeFragment newInstance(WelcomeScreen welcomeScreen, boolean isBigScreen) {
        WelcomeFragment fragment = new WelcomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_ID, welcomeScreen.getId());
        args.putString(ARG_SECTION_NAME, welcomeScreen.getName());
        args.putString(ARG_SECTION_IMAGE, isBigScreen ? welcomeScreen.getImageTablet() : welcomeScreen.getImagePhone());
        args.putString(ARG_SECTION_COLOR, isBigScreen ? welcomeScreen.getBackgroundColorTablet() : welcomeScreen.getBackgroundColorPhone());
        args.putString(ARG_SECTION_URL, welcomeScreen.getUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @BindView((R.id.welcomeImg))
    ImageView imageView;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.message)
    TextView message;

    @OnClick(R.id.welcomeImg)
    void onBgClick(View v){
        try {
            if (!TextUtils.isEmpty(url)){
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        id = bundle.getInt(ARG_SECTION_ID);
        text = bundle.getString(ARG_SECTION_NAME);
        img = bundle.getString(ARG_SECTION_IMAGE);
        background = bundle.getString(ARG_SECTION_COLOR);
        url = bundle.getString(ARG_SECTION_URL);

        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        ButterKnife.bind(this, rootView);
        message.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        GlideApp.with(getActivity())
                .load(img)
                //.diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (mListener != null) mListener.onLoadError(id);
                        progressBar.setVisibility(View.GONE);
                        message.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
        rootView.setBackgroundColor(Color.parseColor(background));
//            mListener.onLoadError(screen.getImage(), -1);
        return rootView;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLoadError(int id);
    }
}