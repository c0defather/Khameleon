package c0defather.chameleon;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by kuanysh on 3/29/18.
 */

public class ScreenUtils {
    public static int height;
    public static int width;
    public static int convertDpToPx(Context context, int dp){
        return Math.round(dp*(context.getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }
}
