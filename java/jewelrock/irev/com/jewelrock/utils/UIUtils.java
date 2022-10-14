package jewelrock.irev.com.jewelrock.utils;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

/**
 * Created by Yuri Peremetov on 03.09.2017.
 */

public class UIUtils {
    public static void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getWindow().getDecorView();
        if (view == null) return;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(DialogFragment dialog) {
        if (dialog == null) return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) dialog.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = Objects.requireNonNull(dialog.getDialog().getWindow()).getDecorView();
        if (view == null) return;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getWindow().getDecorView();
        if (view == null) return;
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
