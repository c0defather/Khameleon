package c0defather.chameleon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by c0defather on 3/29/18.
 */

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 1404;
    private ImageButton chameleon;
    private EditText urlEditText;
    private Intent service;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean foreground = false;
    private Handler statusHandler = new Handler();
    private Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (foreground)
                chameleon.setImageResource(ChameleonService.isRunning ? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
            statusHandler.postDelayed(this, 500);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new Intent(this, ChameleonService.class);
        service.setFlags(FLAG_ACTIVITY_NEW_TASK);
        preferences = getSharedPreferences(SharedPref.NAME, MODE_PRIVATE);
        editor = preferences.edit();

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
    }

    /**
     * Set and initialize the view elements.
     */
    private void initializeView() {
        urlEditText = (EditText) findViewById(R.id.urlEditText);
        chameleon = (ImageButton) findViewById(R.id.chameleon);

        chameleon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatus(!ChameleonService.isRunning);
            }
        });
        ((FrameLayout)urlEditText.getParent()).setVisibility(ChameleonService.isRunning ? View.VISIBLE : View.GONE);
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editor.putString(SharedPref.URL, editable.toString()).commit();
            }
        });
        String url = preferences.getString(SharedPref.URL, "http://github.com/c0defather");
        urlEditText.setText(url);
        statusHandler.post(statusChecker);
    }

    private void changeStatus(boolean status) {
        chameleon.setImageResource(status? R.mipmap.chameleon_on : R.mipmap.chameleon_off);
        ((FrameLayout)urlEditText.getParent()).setVisibility(status ? View.VISIBLE : View.GONE);

        if (status) {
            startService(service);
        } else {
            stopService(service);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            // Settings activity never returns proper value so instead check with following method
            if (Settings.canDrawOverlays(this)) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        foreground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        foreground = false;
    }
}
