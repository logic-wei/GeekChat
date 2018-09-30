package com.logic.geekchat.protocol.packer;

import com.xuhao.android.common.interfacies.client.msg.ISendable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JsonPacker implements IBodyPacker {
    protected JSONObject json = null;

    protected JsonPacker() { }

    public JsonPacker(byte[] rowData) throws UnsupportedEncodingException, JSONException {
        String str = new String(rowData, "UTF-8");
        json = new JSONObject(str);
    }

    public JsonPacker(JSONObject json) {
        this.json = json;
    }

    public byte[] packBodyBytes() throws UnsupportedEncodingException {
        return json.toString().getBytes("UTF-8");
    }

    public JSONObject getJson() {
        return this.json;
    }

    public String getMethods() throws JSONException {
        return json.getString("method");
    }

    @Override
    public int getPackerType() {
        return PackerFactory.PROTOCAL_TYPE_JSON;
    }
}
