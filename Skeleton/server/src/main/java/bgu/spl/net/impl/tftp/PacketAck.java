package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;

public class PacketAck{

    ByteArrayOutputStream data;

    int opCode;

    boolean endInZero;

    public PacketAck(short opCode, short blockNumber){
        this.opCode = opCode;
        this.endInZero = false;
        data = new ByteArrayOutputStream();
        data.write(0);
        data.write(opCode);
        data.write(blockNumber >> 8);
        if (blockNumber != 0){
            data.write(blockNumber & 0xFF);
        }
        else data.write(0);

    }

    public byte[] getPacket(){
        return data.toByteArray();
    }

}