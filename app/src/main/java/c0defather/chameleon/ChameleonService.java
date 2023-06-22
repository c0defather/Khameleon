package c0defather.chameleon;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * Created by c0defather on 3/29/18.
 */

public class ChameleonService extends Service {

    public static boolean isRunning;

    private WindowManager.LayoutParams topParams;
    private WindowManager.LayoutParams edgeParams;
    private RelativeLayout topView;
    private UnmaskRelativeLayout contentView;
    private View topGrab;
    private View edge;
    private WebView webView;
    private SeekBar seekBar;
    private WindowManager windowManager;
    private GestureDetectorCompat gestureDetector;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        isRunning = true;
        initScreenUtils();
        preferences = getSharedPreferences(SharedPref.NAME, MODE_PRIVATE);
        editor = preferences.edit();
        gestureDetector = new GestureDetectorCompat(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (topParams.width == 0) {
                    topParams.width = ScreenUtils.width;
                    topView.setVisibility(View.VISIBLE);
                    windowManager.updateViewLayout(topView, topParams);
                } else {
                    topParams.width = 0;
                    windowManager.updateViewLayout(topView, topParams);
                    topView.setVisibility(View.GONE);
                }
                return true;
            }
        });
        initViews();
        initOnClicks();
        initOnTouches();
        initProgress();
    }

    private void initViews() {
        topView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.top, null);
        contentView = (UnmaskRelativeLayout) topView.findViewById(R.id.content);
        topGrab = topView.findViewById(R.id.grab);
        seekBar = (SeekBar) topView.findViewById(R.id.alphaSeek);

        topParams = new WindowManager.LayoutParams(
                ScreenUtils.width,
                ScreenUtils.height/2,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowManager.addView(topView, topParams);


        edge = new View(getApplicationContext());
        edgeParams = new WindowManager.LayoutParams(
                ScreenUtils.width/20,
                ScreenUtils.height,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        edgeParams.gravity = Gravity.RIGHT;
        windowManager.addView(edge, edgeParams);
    }

    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
    }

    private void initOnClicks() {
        topView.findViewById(R.id.webButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                stopSelf();
                return true;
            }
        });
        topView.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView == null) {
                    webView = new WebView(getApplicationContext());
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    contentView.addView(webView);
                    webView.setLayoutParams(layoutParams);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(preferences.getString(SharedPref.URL, "http://github.com/c0defather"));
                } else {
                    contentView.removeView(webView);
                    webView.destroy();
                    webView = null;
                }
            }
        });
    }

    private void initProgress() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                topView.findViewById(R.id.content).setAlpha((float) (i/100.0));
                editor.putInt(SharedPref.ALPHA, i).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int progress = preferences.getInt(SharedPref.ALPHA, 50);
        topView.findViewById(R.id.content).setAlpha((float) (progress/100.0));
        seekBar.setProgress(progress);
    }

    private void initOnTouches() {
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        int x = (int)motionEvent.getRawX();
                        int y = (int)motionEvent.getRawY();
                        contentView.setUnmaskCircle(new Circle(x-ScreenUtils.width/6,y,ScreenUtils.width/6));
                        break;
                    case MotionEvent.ACTION_UP:
                        contentView.setUnmaskCircle(null);
                }
                contentView.invalidate();
                return true;
            }
        });
        edge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        topGrab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        topParams.height = Math.max((int) motionEvent.getRawY(), ScreenUtils.convertDpToPx(ChameleonService.this, 50));
                        windowManager.updateViewLayout(topView, topParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (topView != null) windowManager.removeView(topView);
        if (edge != null) windowManager.removeView(edge);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Проверяем ориентацию экрана
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onDestroy();
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            onCreate();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            onDestroy();
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            onCreate();
        }
    }
}
