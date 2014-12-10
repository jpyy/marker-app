package com.example.kkenttal.markerapplication;

import java.util.HashMap;

/**
 * Created by Jyri on 29.11.2014.
 */
public class CoordinateMessage {
    public static final String HEADER_EVENT = "Event";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_PUPIL_DETECTED = "pupil_detected";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_TYPE = "type";
    public static final String SEPARATOR = ":";
    public static final String VALUE_TYPE = "android_coordinates";

    public double x;
    public double y;
    public boolean pupilDetected;
    public double timestamp;
    public String type;

    public CoordinateMessage() {
        x = y = timestamp = 0f;
        pupilDetected = false;
        type = "";
    }

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
                !keyValues.containsKey(KEY_Y) ||
                !keyValues.containsKey(KEY_PUPIL_DETECTED) ||
                !keyValues.containsKey(KEY_TIMESTAMP))
            return null;
        CoordinateMessage result = new CoordinateMessage();
        result.type = keyValues.get(KEY_TYPE).trim();
        if (!result.type.equals(VALUE_TYPE))
            return null;
        try {
            result.x = Double.parseDouble(keyValues.get(KEY_X));
            result.y = Double.parseDouble(keyValues.get(KEY_Y));
            result.pupilDetected = Boolean.parseBoolean(keyValues.get(KEY_PUPIL_DETECTED));
            result.timestamp = Double.parseDouble(keyValues.get(KEY_TIMESTAMP));
        } catch (NumberFormatException e) {
            return null;
        }
        return result;
    }
}
