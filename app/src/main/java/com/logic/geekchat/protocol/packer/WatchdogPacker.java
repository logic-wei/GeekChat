package com.logic.geekchat.protocol.packer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class WatchdogPacker extends JsonPacker {
    public WatchdogPacker() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", "com.heartbeat.request");
        super.json = jsonObject;
    }
}
