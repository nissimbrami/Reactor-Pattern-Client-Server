package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;

public class Packet2Byte{

    int opCode;

    boolean endInZero;


    ByteArrayOutputStream data;

    public Packet2Byte(int opCode){
        this.opCode = opCode;
        this.endInZero = false;
        data = new ByteArrayOutputStream();
        data.write(0);
        data.write(opCode);
    }

    public byte[] getPacket(){
        return data.toByteArray();
    }

}