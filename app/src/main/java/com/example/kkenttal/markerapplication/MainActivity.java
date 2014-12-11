package com.example.kkenttal.markerapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;


public class MainActivity extends Activity {


    //private boolean mIsBound;
    Thread mCoordinateWorkerThread;
    CoordinateWorker mCoordinateWorker;
    Handler mCoordinateHandler;
    Double mBlinkTimestamp;
    Double mMinBlinkTime;
    Double mMaxBlinkTime;

    final String phoneUriPrefix = "tel:";
    String mPhoneNumber;

    TextView mCoordView;
    View mCrosshairView;
    private WindowManager mWm;

    private static final int[] BUTTON_RESOURCES = {
            R.id.button1, R.id.button2, R.id.button3, 
            R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9,
            R.id.button0, R.id.button_call, R.id.button_clear};
    private HashMap<RectF, Button> mButtons;

    private void updateButtonRects() {
        DisplayMetrics dm = new DisplayMetrics();
        mWm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        mButtons = new HashMap<RectF, Button>();
        for (int res : BUTTON_RESOURCES) {
            Button b = (Button) findViewById(res);
            b.setFocusableInTouchMode(true);
            int[] position = new int[2];
            b.getLocationOnScreen(position);
            int width = b.getWidth();
            int height = b.getHeight();

            RectF screenRect = new RectF(
                    (float)position[0] / screenWidth,
                    (float)position[1] / screenHeight,
                    ((float)position[0] + width) / screenWidth,
                    ((float)position[1] + height) / screenHeight
            );
            mButtons.put(screenRect, b);
        }
    }

    private void bindNumberButton(int id, final char number) {
        View v = findViewById(id);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneNumber += number;
                mCoordView.setText(mPhoneNumber);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);
        mPhoneNumber = "";
        bindNumberButton(R.id.button0, '0');
        bindNumberButton(R.id.button1, '1');
        bindNumberButton(R.id.button2, '2');
        bindNumberButton(R.id.button3, '3');
        bindNumberButton(R.id.button4, '4');
        bindNumberButton(R.id.button5, '5');
        bindNumberButton(R.id.button6, '6');
        bindNumberButton(R.id.button7, '7');
        bindNumberButton(R.id.button8, '8');
        bindNumberButton(R.id.button9, '9');

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            mMinBlinkTime = Double.parseDouble(sp.getString(getString(R.string.pref_blinktime_min), "1000"))
                    / 1000.0;
        } catch (NumberFormatException e) {
            mMinBlinkTime = 1.0;
        }
        try {
            mMaxBlinkTime = Double.parseDouble(sp.getString(getString(R.string.pref_blinktime_max), "3000"))
                    / 1000.0;
        } catch (NumberFormatException e) {
            mMaxBlinkTime = 5.0;
        }
        mCrosshairView = createOverlayView();
        //doBindService();
        mCoordView = (TextView)findViewById(R.id.coord_textview);
        mCoordView.setText(mPhoneNumber);
        findViewById(R.id.button_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhoneNumber.length() > 0) {
                    startActivity(new Intent(Intent.ACTION_CALL)
                            .setData(Uri.parse(phoneUriPrefix + mPhoneNumber)));
                    mPhoneNumber = "";
                    mCoordView.setText(mPhoneNumber);

                }
            }
        });

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneNumber = "";
                mCoordView.setText(mPhoneNumber);
            }
        });
        mCoordinateHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                onCoordinatesUpdated();
                return true;
            }
        });
        mCoordinateWorker = new CoordinateWorker(getZmqUrl(), mCoordinateHandler);
        mCoordinateWorkerThread = new Thread(mCoordinateWorker);
        mCoordinateWorkerThread.start();
        mBlinkTimestamp = null;

        updateButtonRects();

    }

    private View createOverlayView() {
        ImageView iv = new ImageView(this);
        Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setStrokeWidth(2.0f);
        bm.eraseColor(Color.TRANSPARENT);
        c.drawLine(50, 0, 50, 100, blue);
        c.drawLine(0, 50, 100, 50, blue);
        c.save();
        iv.setImageBitmap(bm);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWm.addView(iv, lp);

        return iv;
    }

    private String getZmqUrl() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_zeromq_url", "");
    }

    private void onCoordinatesUpdated() {
        CoordinateMessage c = mCoordinateWorker.getNormalizedCoordinates();
        if (c.pupilDetected) {
            if (mBlinkTimestamp != null) {
                double dtBlink = c.timestamp - mBlinkTimestamp;
                if ( dtBlink >= mMinBlinkTime && dtBlink <= mMaxBlinkTime) {

                    getCurrentFocus().performClick();
                }
                mBlinkTimestamp = null;
            }
            //mCoordView.setText("x: " + c.x + " y: " + c.y);
            //mCrosshairView.setX(c.x * 10);
            //mCrosshairView.setY(c.y * 10);
            DisplayMetrics dm = new DisplayMetrics();
            mWm.getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;


            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    (int) ((c.x - 0.5) * screenWidth),
                    (int) ((c.y - 0.5) * screenHeight),
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
            mWm.updateViewLayout(mCrosshairView, lp);

            updateButtonRects();
            for (RectF key : mButtons.keySet()) {
                if (key.contains((float)c.x, (float)c.y)) {
                    mButtons.get(key).requestFocus();
                }
            }
        } else {
            //mCoordView.setText("no pupil");
            if (mBlinkTimestamp == null)
                mBlinkTimestamp = c.timestamp;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //doUnbindService();
        mCoordinateWorker.quit();
        try {
            mCoordinateWorkerThread.join(500);
            mCoordinateWorkerThread.interrupt();
            mCoordinateWorkerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*private PupilService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((PupilService.LocalBinder)service).getService();

            // Tell the user about this for our demo.
            Toast.makeText(MainActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Toast.makeText(MainActivity.this, "Service disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };


    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(MainActivity.this,
                PupilService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    */

}
