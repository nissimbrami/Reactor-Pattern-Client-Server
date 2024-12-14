package bgu.spl.net.impl.tftp;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.ByteOrder;
import java.util.concurrent.CopyOnWriteArrayList;
import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    private ByteBuffer opBuffer = ByteBuffer.allocate(2);
    private boolean isOpCode ;
    private int opCode ;
    private byte[] bytes ; // 1KB buffer
    private int index ;
    private short DataPacketSize;

    public TftpEncoderDecoder (){                     //    constructor
        this.isOpCode = false ;
        this.bytes = new byte[1 << 10];
        this.index = 0 ;
        this.opCode = 0;
        this.DataPacketSize = 0;
    }
    @Override
    public byte[] decodeNextByte(byte nextByte) {
        if (!isOpCode) {
            this.opCode = 0;
            opBuffer.put(nextByte);
            if ((short)nextByte != 0) {
                isOpCode = true;
                opBuffer.flip(); // Prepare for reading from the buffer
                this.opCode = (short)nextByte;  // Read 2 bytes as a short and convert to int
                if (opCode == 6 || opCode == 10) {
                    isOpCode = false;
                }
                opBuffer.clear();
            }
        }
        else {
            // Opcode is ready, collect data
            pushByte(nextByte);
            if (opCode == 3) {
                if (index == 2) {
                    this.DataPacketSize = (short) (((short) bytes[0]) << 8 | (short) (bytes[1]) & 0x00ff);
                }
                else if (index == DataPacketSize + 4) {
                    this.DataPacketSize = 0;
                    return popByte();
                }
            }
            else if (opCode == 4) {
                System.out.println("1");
                if (index == 2) {
                    System.out.println("2");
                    return popByte();
                }
            }
            else if (nextByte == 0 && index != 1) { // Assuming the message is terminated by a zero byte
                byte[] msg = new byte[index - 1];
                System.arraycopy(popByte(), 0, msg, 0, msg.length);
                return msg;
            }
        }
        return null; // Opcode not fully formed yet
    }
    private void pushByte(byte nextByte) {
        if (index >= bytes.length) {
            byte[] newBytes = new byte[index * 2];
            System.arraycopy(bytes, 0, newBytes, 0, index);
            bytes = newBytes;
        }
        bytes[index++] = nextByte;
    }
    private byte[] popByte() {
        byte[] result = new byte[index];
        System.arraycopy(bytes, 0, result, 0, index);
        index = 0;
        isOpCode = false ;
        bytes = new byte[1 << 10];
        return result;
    }
    @Override
    public byte[] encode(byte[] message) {
        return message;
    }

    public boolean hasOpCode(){
        return this.isOpCode;
    }
    public int getOpCode(){
        return this.opCode;
    }
}