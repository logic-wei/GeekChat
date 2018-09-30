package com.logic.geekchat;

import java.nio.ByteBuffer;

public class Util {
    public static String bytesToHexString(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bytes) {
            String hexByte = Integer.toHexString(b & 0xff).toUpperCase();
            if (hexByte.length() == 1) {
                stringBuffer.append('0');
                stringBuffer.append(hexByte);
            } else if (hexByte.length() == 2) {
                stringBuffer.append(hexByte);
            }
        }
        return stringBuffer.toString();
    }

    @Deprecated
    public static byte[] hexStringToBytes(String hex) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 1) {
            //byteBuffer.put(Byte.valueOf(new String(hex.charAt(i)), 16));
        }
        return byteBuffer.array();
    }
}
