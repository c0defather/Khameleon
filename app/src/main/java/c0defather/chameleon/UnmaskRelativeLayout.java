package c0defather.chameleon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by kuanysh on 3/30/18.
 */

public class UnmaskRelativeLayout extends RelativeLayout {
    private Circle unmaskCircle;
    private Paint unmaskPaint = new Paint();
    {
        unmaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public UnmaskRelativeLayout(Context context) {
        super(context);
    }

    public UnmaskRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnmaskRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * Add an unmasking rectangle to this view's background.
     *
     * @param circle
     *            a rectangle used to unmask the background
     */
    public void setUnmaskCircle(Circle circle) {
        unmaskCircle = circle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (unmaskCircle != null) {
            canvas.drawColor(Color.BLACK);
            canvas.drawCircle(unmaskCircle.x, unmaskCircle.y, unmaskCircle.r, unmaskPaint);
        }
    }
}
