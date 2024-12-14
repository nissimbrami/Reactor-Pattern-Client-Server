package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PacketBcast{

    String FileName;

    int opCode;

    boolean endInZero;


    int errorCode;

    int size;

    ByteArrayOutputStream data;

    public PacketBcast(int opCode, int errorCode,int addOrDelete, String FileName){
        this.opCode = opCode;
        this.errorCode = errorCode;
        this.endInZero = true;
        this.FileName = FileName;
        this.data = new ByteArrayOutputStream();
        byte[] UTF8_message = FileName.getBytes(StandardCharsets.UTF_8);
        int size = 2 + 1 + 1 + UTF8_message.length;
        data.write(0);
        data.write(opCode);
        data.write(addOrDelete);
        try {
            data.write(UTF8_message);
        }catch (Exception ignore){}
        data.write(0);
    }
    public byte[] getPacket(){
        return data.toByteArray();
    }




}
