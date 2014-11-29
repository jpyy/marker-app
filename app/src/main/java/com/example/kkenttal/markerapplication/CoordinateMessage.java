package com.example.kkenttal.markerapplication;

import java.util.HashMap;

/**
 * Created by Jyri on 29.11.2014.
 */
public class CoordinateMessage {
    public static final String HEADER_EVENT = "Event";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_TYPE = "type";
    public static final String SEPARATOR = ":";
    public static final String VALUE_TYPE = "android_coordinates";

    private float mx;
    private float my;
    private String mType;

    public static CoordinateMessage fromMessage(String message) {
        String[] lines = message.split("\n");
        if (lines == null || lines.length < 1 || !lines[0].startsWith(HEADER_EVENT))
            return null;
        HashMap<String, String> keyValues = new HashMap<String, String>();
        for ( int i = 1; i < lines.length; i++) {
            String[] kv = lines[i].split(SEPARATOR, 2);
            if (kv == null || kv.length != 2)
                continue;
            keyValues.put(kv[0], kv[1]);
        }
        if (!keyValues.containsKey(KEY_TYPE) ||
                !keyValues.containsKey(KEY_X) ||
                !keyValues.containsKey(KEY_Y))
            return null;
        CoordinateMessage result = new CoordinateMessage();
        result.mType = keyValues.get(KEY_TYPE).trim();
        if (!result.mType.equals(VALUE_TYPE))
            return null;
        try {
            result.mx = Float.parseFloat(keyValues.get(KEY_X));
            result.my = Float.parseFloat(keyValues.get(KEY_Y));
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }

    public float getX() {
        return mx;
    }

    public void setX(float mx) {
        this.mx = mx;
    }

    public float getY() {
        return my;
    }

    public void setY(float my) {
        this.my = my;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }
}
