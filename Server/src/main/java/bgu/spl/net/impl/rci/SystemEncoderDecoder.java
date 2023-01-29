package bgu.spl.net.impl.rci;

import bgu.spl.net.MSG;
import bgu.spl.net.api.MessageEncoderDecoder;
/*import sun.security.util.ArrayUtil;*/

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SystemEncoderDecoder implements MessageEncoderDecoder<MSG> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short opcode = 0;
    private boolean pulledUsername = false;
    private String username = "";
    private boolean opcodeExtract = false;

    @Override
    public MSG decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison

        if(opcode == 8 && nextByte == '\0'){
            return new MSG(opcode,username,null,(short)0,popString(),(short)0);

        }
        if(opcode == 1 | opcode == 2 | opcode == 3){
            if(nextByte == '\0' && !pulledUsername){
                pulledUsername = true;
                username = popString();
            }
            if(nextByte == '\0' && len != 0)
                return new MSG(opcode, username, popString(), (short)0,"", (short)0);
        }

        pushByte(nextByte);
        if(len == 2 & !opcodeExtract){
            opcode = bytesToShort(bytes);
            opcodeExtract = true;
        }

        if(opcode == 4 | opcode == 11){
            return new MSG(opcode,null,null,(short)0,"",(short)0);
        }

        if((opcode == 5 | opcode == 6 | opcode == 7 | opcode == 9 | opcode == 10) && len == 2){
            return new MSG(opcode,null,null,bytesToShort(bytes),"",(short)0);
        }
        return null; //not a line yet
    }

    @Override
    public byte[] encode(MSG msg) {
        opcode = 0;
        pulledUsername = false;
        username = "";
        opcodeExtract = false;

        short opcode = msg.getOpcode();
        short messageOpcode = msg.getReturnOP();
        byte[] opcodeArr =shortToBytes(msg.getOpcode());
        byte[] messageOpcodeArr = shortToBytes(msg.getReturnOP());
        byte[] stringArr = ('\n' + msg.getReturnSTR() + '\0').getBytes(); //uses utf8 by default

        if (opcode == 12 && ( messageOpcode == 6 | messageOpcode == 7 | messageOpcode == 8 | messageOpcode == 9 | messageOpcode == 11)) {
            return appendArray(appendArray(opcodeArr,messageOpcodeArr),stringArr);
        }
        return appendArray(opcodeArr,messageOpcodeArr);

    }
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;

    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes,0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        len = 0;
        return result;
    }
    public short bytesToShortCourse(byte[] byteArr)
    {
        short result = (short)((byteArr[2] & 0xff) << 8);
        result += (short)(byteArr[3] & 0xff);
        len = 0;
        return result;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }
    private  byte[] appendArray(byte[] array1, byte[] array2){
        int len = array1.length + array2.length;
        byte[] array = new byte[len];
        for(int i = 0; i < array1.length; i++)
            array[i] = array1[i];
        for(int i = array1.length; i < len; i++)
            array[i] = array2[i - array1.length];
        return array;
    }
}
