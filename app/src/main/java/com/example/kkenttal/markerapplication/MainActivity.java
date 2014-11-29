package com.example.kkenttal.markerapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
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
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity {


    //private boolean mIsBound;
    Thread mCoordinateWorkerThread;
    CoordinateWorker mCoordinateWorker;
    Handler mCoordinateHandler;
    TextView mCoordView;
    View mCrosshairView;
    private WindowManager mWm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid);
        mCrosshairView = createOverlayView();
        //doBindService();
        mCoordView = (TextView)findViewById(R.id.coord_textview);
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
        PointF c = mCoordinateWorker.getNormalizedCoordinates();
        mCoordView.setText("x: " + c.x + " y: " + c.y);
        //mCrosshairView.setX(c.x * 10);
        //mCrosshairView.setY(c.y * 10);
        DisplayMetrics dm = new DisplayMetrics();
        mWm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                (int)((c.x - 0.5) * screenWidth),
                (int)((c.y - 0.5) * screenHeight),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        mWm.updateViewLayout(mCrosshairView, lp);
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
