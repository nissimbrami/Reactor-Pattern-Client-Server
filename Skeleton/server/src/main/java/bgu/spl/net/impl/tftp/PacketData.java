package bgu.spl.net.impl.tftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PacketData{

    short opCode;

    boolean endInZero;
    List<byte[]> dataPackets;
    int index ;
    private short numberOfPackets;

    public PacketData(short opCode, Path filePath) {
        this.opCode = opCode;
        this.endInZero = false;
        this.index=  0 ;
        this.numberOfPackets = 0;
        this.dataPackets = new ArrayList<>();
        byte[] fileData;
        try {
            fileData = Files.readAllBytes(filePath);
        } catch (IOException ignored) {
            fileData = null;
        }

        if (fileData != null) {
            this.numberOfPackets = (short)((fileData.length / 512)+1); // Rounds up
            short blockNumber = 1; // Starting block number
            for (int i = 0; i < numberOfPackets; i++) {
                int start = (i*512);
                short length = (short)(Math.min(512, fileData.length - start));
                ByteArrayOutputStream packetStream = new ByteArrayOutputStream(6 + length); // Adjusted size
                // Constructing the packet with opCode and blockNumber
                packetStream.write(0); // Opcode high byte (assuming opCode < 256 for simplicity)
                packetStream.write(opCode); // Opcode low byte
                packetStream.write((byte) (length >> 8));
                packetStream.write((byte) (length & 0xFF)); // Block number low byte
                packetStream.write((byte)(blockNumber >> 8)); // Block number high byte
                packetStream.write((byte) (blockNumber & 0xFF)); // Block number low byte
                // Adding file data to the packet
                packetStream.write(fileData, start, length);
                dataPackets.add(packetStream.toByteArray());
                blockNumber++; // Increment block number for the next packet
            }
        }
    }
    public byte[] getPacket(int index){
        System.out.println("List Size: " + dataPackets.size());
        return dataPackets.get(index);
    }
    public int getDataPacketLength(){
        return dataPackets.size();
    }


}