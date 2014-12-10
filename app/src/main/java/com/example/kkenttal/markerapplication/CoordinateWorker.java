package com.example.kkenttal.markerapplication;

import android.os.Handler;
import android.util.Log;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Jyri on 29.11.2014.
 */
public class CoordinateWorker implements Runnable {
    private static final String LOG_TAG = CoordinateWorker.class.getSimpleName();
    volatile boolean mQuit = false;
    AtomicReference<CoordinateMessage> mNormalizedCoordinates;
    String mUrl;
    Handler mParent;

    public CoordinateWorker(String url, Handler parent) {
        mNormalizedCoordinates = new AtomicReference<CoordinateMessage>(new CoordinateMessage());
        mUrl = url;
        mParent = parent;
    }

    @Override
    public void run() {
        Log.v(LOG_TAG, "Worker started");
        ZContext context = new ZContext();
        context.setMain(true);
        ZMQ.Socket subscriber = context.createSocket(ZMQ.SUB);
        Log.v(LOG_TAG, "Connecting to " + mUrl);
        try {
            subscriber.connect(mUrl);
        } catch (Exception e) {
            context.destroy();
            e.printStackTrace();
            return;
        }
        byte[] filter = {};
        subscriber.subscribe(filter);
        Log.v(LOG_TAG, "Subscribed to: " + new String(filter));
        while (!mQuit) {
            String message = subscriber.recvStr(ZMQ.DONTWAIT);
            if (message != null) {
                CoordinateMessage cm = CoordinateMessage.fromMessage(message);
                if (cm != null) {
                    //PointF coords = new PointF(cm.getX(), cm.getY());
                    mNormalizedCoordinates.set(cm);
                    mParent.sendEmptyMessage(0);
                }
            } else {
                Thread.yield();
            }
        }
        Log.v(LOG_TAG, "Worker stopping...");
        //subscriber.close();
        context.destroy();
        Log.v(LOG_TAG, "Worked stopped.");
    }

    public CoordinateMessage getNormalizedCoordinates() {
        return mNormalizedCoordinates.get();
    }

    public void quit() {
        mQuit = true;
    }
}
