package com.logic.geekchat.protocol.packer;

import java.io.UnsupportedEncodingException;

public interface IBodyPacker {
    public byte[] packBodyBytes() throws UnsupportedEncodingException;
    public int getPackerType();
}
