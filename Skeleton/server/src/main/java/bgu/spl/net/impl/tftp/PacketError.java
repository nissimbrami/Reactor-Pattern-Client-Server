package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PacketError{

    String errorMessage;

    short errorCode;

    int size;

    short opCode;

    boolean endInZero;

    ByteArrayOutputStream data;

    public PacketError(short opCode, short errorCode, String message){
        this.opCode = opCode;
        this.errorCode = errorCode;
        this.endInZero = true;
        this.errorMessage = message;

        byte[] UTF8_message = message.getBytes(StandardCharsets.UTF_8);
        //int size = 2 + 2 + 1 + UTF8_message.length;
        this.data = new ByteArrayOutputStream(4 + UTF8_message.length + 1);
        data.write(0);
        data.write(opCode);
        data.write(errorCode >> 8);
        data.write(errorCode & 0xFF);
        try {
            data.write(UTF8_message);
        }catch (Exception ignore){}
        data.write(0);
    }
    public byte[] getPacket(){
        return data.toByteArray();
    }


    public void print(){
        System.out.println(opCode);
        System.out.println(errorCode);
        System.out.println(errorMessage);

        for (int i = 0; i < size; i++){
            System.out.println(getPacket()[i]);
        }

    }




}
