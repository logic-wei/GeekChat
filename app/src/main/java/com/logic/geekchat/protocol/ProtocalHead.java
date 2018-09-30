package com.logic.geekchat.protocol;

import android.util.Log;

import com.logic.geekchat.protocol.packer.IBodyPacker;
import com.logic.geekchat.protocol.packer.PackerFactory;
import com.xuhao.android.common.interfacies.client.msg.ISendable;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class ProtocalHead implements ISendable {
    private static final String TAG = "ProtocalHead";
    private static final int PROTOCAL_HEAD_LENGHT = 8;
    private static final int PROTOCAL_PACKER_LENGTH_MAX = 4096;
    private static final int PROTOCAL_CRC32_OFFSET = 4;
    private int mPackerType = 0;
    private long mCrc = 0;
    private short mBodyLengthInHead = 0;
    private IBodyPacker bodyPacker = null;

    public int getHeadLength() {
        return PROTOCAL_HEAD_LENGHT;
    }

    public int setHeadData(byte[] head) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(head);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.mCrc = byteBuffer.getInt() & 0xFFFFFFFFL;
        this.mBodyLengthInHead = byteBuffer.getShort();
        this.mPackerType = byteBuffer.getShort();
        this.bodyPacker = null;
        return 0;
    }

    public int setDataByBytes(byte[] head, byte[] body) throws UnsupportedEncodingException, JSONException, BodyLengthException, CRCMistakeException {
        this.setHeadData(head);
        this.bodyPacker = PackerFactory.buildBodyPacker(this.mPackerType, body);

        if(this.mBodyLengthInHead != body.length) {
            throw new BodyLengthException();
        }

        CRC32 crc32 = new CRC32();
        crc32.update(head, PROTOCAL_CRC32_OFFSET,  PROTOCAL_HEAD_LENGHT - PROTOCAL_CRC32_OFFSET);
        crc32.update(body, 0,  body.length);
        if(crc32.getValue() != this.mCrc) {
            throw new CRCMistakeException();
        }
        return 0;
    }

    public int setBodyPacker(IBodyPacker packer) {
        this.bodyPacker = packer;
        return 0;
    }

    public IBodyPacker getBodyPacker() {
        return this.bodyPacker;
    }

    public int getPackerType() {
        return this.mPackerType;
    }

    public void setPackerType(int packerType) {
        this.mPackerType = packerType;
    }

    public int getBodyLength() throws UnsupportedEncodingException{
        return this.bodyPacker.packBodyBytes().length;
    }

    public int getBodyLengthInHead() {
        return this.mBodyLengthInHead;
    }

    @Override
    public byte[] parse() {
        byte [] packerBytes = null;
        try {
            packerBytes = this.bodyPacker.packBodyBytes();
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(packerBytes.length + PROTOCAL_HEAD_LENGHT);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(0);
        byteBuffer.putShort((short)packerBytes.length);
        byteBuffer.putShort((short)this.mPackerType);
        byteBuffer.put(packerBytes);

        CRC32 crc32 = new CRC32();
        crc32.update(byteBuffer.array(), PROTOCAL_CRC32_OFFSET, packerBytes.length + PROTOCAL_HEAD_LENGHT - PROTOCAL_CRC32_OFFSET);

        byteBuffer.putInt(0, (int)crc32.getValue());
        return byteBuffer.array();
    }
}
