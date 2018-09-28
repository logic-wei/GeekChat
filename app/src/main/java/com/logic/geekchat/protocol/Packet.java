package com.logic.geekchat.protocol;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class Packet implements Serializable{
    public static final int SIZE_MAX = 1024;
    public static final int SIZE_HEAD = 8;

    public static final int SIZE_CRC32 = 4;
    public static final int SIZE_LENGTH = 2;
    public static final int SIZE_TYPE = 2;

    public static final int INDEX_CRC32 = 0;
    public static final int INDEX_LENGTH = 4;
    public static final int INDEX_TYPE = 6;
    public static final int INDEX_DATA = 8;

    public static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    public static class CrcErrorException extends Exception {}

    private long mCrc32;//4byte
    private int mLength;//2byte
    private int mType;//2byte
    private byte[] mRawData;

    public Packet() { }

    public long getCrc32() {
        return mCrc32;
    }

    public int getLength() {
        return mLength;
    }

    public int getType() {
        return mType;
    }

    public String getStringData() {
        try {
            return new String(mRawData,"UTF-8");
        } catch (Exception e) {
            Log.e("getData", e.toString());
            return "";
        }
    }

    public byte[] getData() {
        return mRawData;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setData(String data) {
        try {
            mRawData = data.getBytes("UTF-8");
        } catch (Exception e) {
            Log.e("setData", e.toString());
        }
    }

    public void setData(byte[] data) {
        mRawData = data;
    }

    private void updateLength() {
        mLength = mRawData.length;
    }

    public byte[] pack() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(SIZE_HEAD + mRawData.length);

        //update length
        updateLength();

        byteBuffer.order(BYTE_ORDER);
        byteBuffer.putInt((int) mCrc32);
        byteBuffer.putShort((short) mLength);
        byteBuffer.putShort((short) mType);
        for (byte b : mRawData) {
            byteBuffer.put(b);
        }

        //update crc32
        CRC32 crc32Right = new CRC32();
        crc32Right.update(byteBuffer.array(), SIZE_CRC32,
                byteBuffer.array().length - SIZE_CRC32);
        byteBuffer.putInt(INDEX_CRC32, (int) crc32Right.getValue());

        return byteBuffer.array();
    }

    public static Packet unPack(byte[] bytes, int len) throws CrcErrorException {
        if(!isBytesEnough(bytes, len)) {
            return null;
        } else {
            if (!isBytesRight(bytes, len)) {
                throw new CrcErrorException();
            } else {
                return fromBytes(bytes);
            }
        }
    }

    static private Packet fromBytes(byte[] bytes) {
        Packet packet = new Packet();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(BYTE_ORDER);
        packet.mCrc32 = byteBuffer.getInt();
        packet.mLength = byteBuffer.getShort();
        packet.setType(byteBuffer.getShort());
        byte[] dataBuff = new byte[byteBuffer.capacity() - SIZE_HEAD];
        byteBuffer.get(dataBuff, 0, byteBuffer.capacity() - SIZE_HEAD);
        try {
            packet.setData(new String(dataBuff, "UTF-8"));
        } catch (Exception e) {
            Log.e("fromBytes", e.toString());
        }

        return packet;
    }

    private static boolean isBytesEnough(byte[] bytes, int len) {
        if (len < SIZE_HEAD)
            return false;
        return  len >= SIZE_HEAD + ByteBuffer.wrap(bytes).order(BYTE_ORDER).getShort(INDEX_LENGTH);
    }

    private static boolean isBytesRight(byte[] bytes, int len) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(BYTE_ORDER);
        int crc32 = byteBuffer.getInt(INDEX_CRC32);
        CRC32 crc32Right = new CRC32();
        crc32Right.update(bytes, SIZE_CRC32, len - SIZE_CRC32);
        //Log.e("isByteRight", "crc1="+crc32+" crc2="+(int)crc32Right.getValue()+" len="+byteBuffer.array().length);
        Log.e("crc", "org="+Integer.toHexString(crc32)+" rig="+Long.toHexString(crc32Right.getValue()));
        //for (int i = 0; i < len; i += 1) {
        //    Log.e("raw rec", "byte"+i+"=0x"+Integer.toHexString(bytes[i]&0xff));
        //}
        return crc32 == (int) crc32Right.getValue();
    }
}
