package com.logic.geekchat.protocol.packer;

import com.logic.geekchat.protocol.ProtocalHead;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class PackerFactory {
    public static final int PROTOCAL_TYPE_JSON = 0x1;

    public static IBodyPacker buildBodyPacker(int packerType, byte []body) throws JSONException, UnsupportedEncodingException {
        IBodyPacker packer = null;
        switch(packerType) {
            case PROTOCAL_TYPE_JSON:
                packer = new JsonPacker(body);
                break;
        }
        return packer;
    }

}
