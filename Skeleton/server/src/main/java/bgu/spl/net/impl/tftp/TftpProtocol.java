package bgu.spl.net.impl.tftp;
import java.io.*;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.BlockingConnectionHandler;
import java.util.Arrays;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate;
    private int connectionId;
    private PacketData PackDataTemp ;
    private TftpConnections connections;
    private String currFileName ;
    private short block ;
    private boolean rrq;
    private boolean dirq;
    private boolean finishedDirq;
    private byte[] res;
    private FileOutputStream fos;

    private ConcurrentHashMap<Integer, BlockingConnectionHandler> availableUsers;
    private String SERVER_DIRECTORY = "Flies" + File.separator;

    private volatile boolean connected;

    public void start(int connectionId, TftpConnections connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.currFileName="";
        this.PackDataTemp=null;
        this.block = 0;
        this.rrq = false ;
        this.shouldTerminate=false;
        this.fos = null;
        this.dirq = false;
        this.finishedDirq = false;
        this.res = null;
        this.availableUsers = new ConcurrentHashMap<Integer, BlockingConnectionHandler>();
        this.connected = false;
    }


    @Override
    public void process(byte[] message , BlockingConnectionHandler handl , int opcode) throws IOException {
        //System.out.println("inside process");

        if (!connected){
            if (opcode != 7){
                PacketError error = new PacketError((short) 5,(short) 6,"User not logged in");
                handl.send(error.getPacket());
                this.connected = false;
            }
            else{
                handleLOGRQ(this.connectionId, handl, message);
                this.connected = true;
            }
        }
        else {
            switch (opcode) {
                case 1:
                    //if (validateLogIn()) {
                    try {
                        this.currFileName = new String(message, StandardCharsets.UTF_8);
                        if ((new File(SERVER_DIRECTORY + currFileName).exists())) {
                            this.rrq = true;
                            this.PackDataTemp = new PacketData((short) 3,
                                    Paths.get(SERVER_DIRECTORY + currFileName));
                            handleRRQ();
                        }
                    } catch (IOException e) {
                        handleError(0, "somthing get wrong");
                    }

                    //}
                    break;
                case 2:
                    //if (validateLogIn()) {
                    handleWRQ(message);

                    //}
                    break;
                case 3:
                    //if (validateLogIn()) {
                    handleDATA(message);//TODO

                    //}
                    break;
                case 4:
                    //if (validateLogIn()) {
                    handleACK(message);

                    //}
                    break;
                case 5:
                    //if (validateLogIn()) {
                    handleError(0, "");

                    //}
                    break;
                case 6:
                    //if (validateLogIn()) {
                    dirq = true;
                    byte[] files = new byte[1 << 10];
                    int index = 0;
                    File dir = new File(SERVER_DIRECTORY);
                    for (File f : dir.listFiles()) {
                        byte[] name = (f.getName() + '\0').getBytes(StandardCharsets.UTF_8);
                        while (index + name.length >= files.length) {
                            byte[] temp = new byte[files.length * 2];
                            System.arraycopy(files, 0, temp, 0, index);
                            files = temp;
                        }
                        System.arraycopy(name, 0, files, index, name.length);
                        index += name.length;
                    }
                    res = new byte[index - 1];
                    System.arraycopy(files, 0, res, 0, index - 1);
                    handleDIRQ();

                    //}
                    break;
                case 7:
                    handleLOGRQ(this.connectionId, handl, message);
                    break;
                case 8:
                    //if (validateLogIn()) {
                    handleDELRQ(message);

                    //}
                    break;
                case 10:
                    //if (validateLogIn()) {
                    handleDISC();

                    //}
                    break;
                default:
                    PacketError myError = new PacketError((short) 5, (short) 4, "Unknown opcode");
                    connections.send(connectionId, myError.getPacket());
                    //handleError(0, "Unknown opcode");
                    break;
            }
        }

    }

    private void handleWRQ(byte[] message) throws IOException {
        currFileName = new String(message, 0, message.length , StandardCharsets.UTF_8).trim();
        File currFile = new File(SERVER_DIRECTORY + currFileName);
        if(currFile.exists()){
            //System.out.println("FILE NAME ALREDAY EXIST");
            //handleError(0 , "FILE NAME ALREDAY EXIST");
            PacketError myError = new PacketError((short)5,(short)5,"FILE NAME ALREDAY EXIST");
            connections.send(connectionId, myError.getPacket());
        }
        else{
            try {
                currFile.createNewFile();
                fos = new FileOutputStream(currFile);
                sendACK(this.connectionId , (short) 0);
                //handleBCAST(currFileName.getBytes() , 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleLOGRQ(int connectionId , BlockingConnectionHandler handl , byte[] message) throws IOException {
        String username = new String(message,StandardCharsets.UTF_8);
        if(connections.isConnected(connectionId , username)){
            //System.out.println("ERROR USER EXIST");
            PacketError myError = new PacketError((short)5,(short)7,"USER ALREADY LOGGED IN");
            connections.send(connectionId, myError.getPacket());
        }
        else{
            connections.connect(connectionId , handl); // adding the user with his uniqe id to the ConcurrentHashMap
            //System.out.println("setting: " + username + " with id: "+ connectionId);
            handl.setUserName(username);
            availableUsers.put(connectionId , handl);
            sendACK(connectionId,(short)0);
        }
    }
    private void handleACK(byte[] message){
        short blockRecived = (short) (((short) message[0]) << 8 | (short) (message[1]) & 0x00ff);
        if (rrq & blockRecived == block) {
            if (block < PackDataTemp.getDataPacketLength()){
                try {
                    handleRRQ();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                rrq = false;
                block = 0;
                PackDataTemp = null;
            }
        }
        else if (dirq & blockRecived == block) {
            if (finishedDirq) {
                block = 0;
                finishedDirq = false;
                dirq = false;
                res = new byte[1 << 10];
            }
            else {
                handleDIRQ();
            }
        }
    }
    private void handleRRQ() throws IOException {
        //System.out.println("RRQ");
        connections.send(this.connectionId ,PackDataTemp.getPacket(block));
        block++;
    }

    private void sendACK(int connectionId , short data){

        PacketAck myAck = new PacketAck((short)4, data);

        connections.send(connectionId, myAck.getPacket());
    }

    private void handleDATA(byte[] message) {//should check how the op code of the rrq and dirq will recieve by the client
        try {
            short blockNum = (short) ((short) message[2] << 8 | (short) message[3] & 0x00ff);
            byte[] data = new byte[message.length - 4];
            System.arraycopy(message, 4, data, 0, data.length);
            fos.write(data);
            fos.flush();
            String tempFileName = currFileName;
            if (data.length < 512) {
                fos.close();
                fos = null;
                handleBCAST(currFileName.getBytes() , 1);
                currFileName = "";
            }
            sendACK(this.connectionId, blockNum);
        } catch (Exception e) {
            e.printStackTrace();
            // send error
        }
    }

    private void handleError(int errorCode , String errorMessage) throws IOException {
        PacketError myError = new PacketError((short) 5, (short) errorCode, errorMessage);

        connections.send(connectionId, myError.getPacket());
    }
    private void handleDIRQ() {
        byte[] ret;
        int pos = block * 512;
        block++;
        if (res.length - pos < 512) {
            ret = new byte[res.length - pos + 6];
            finishedDirq = true;
        }
        else {
            ret = new byte[518];
        }
        ret[0] = (byte) 0;
        ret[1] = (byte) 3;
        ret[2] = (byte) (((short)ret.length - 6) << 8);
        ret[3] = (byte) (((short)ret.length - 6) & 0x00ff);
        ret[4] = (byte) (block << 8);
        ret[5] = (byte) (block & 0x00ff);
        if (ret.length != 6) {
            System.arraycopy(res, pos, ret, 6, ret.length - 6);
        }
        connections.send(connectionId, ret);
    }


    private void handleDELRQ(byte[] message) throws IOException {//broadact in the end
        byte[] fileName = new byte[message.length];
        System.arraycopy(message, 0, fileName, 0, fileName.length);
        currFileName = new String(fileName, StandardCharsets.UTF_8);
        //System.out.println("name in DELRQ: "+currFileName);
        File delFile = new File(SERVER_DIRECTORY + currFileName);
        if(delFile.delete()) {
            handleBCAST(message , 0);// will delete him and broadcast to anyone
            sendACK(connectionId , (short) 0);
        }
        else{
            //System.out.println("File NOT EXIST");
            //handleError(1, "File not found – RRQ DELRQ of non-existing file.");
            PacketError myError = new PacketError((short) 5, (short) 1, "FILE NOT FOUND");
            connections.send(connectionId, myError.getPacket());
        }
        currFileName = "";
    }
    private void handleBCAST(byte[] message , int delOrAdd) {
        byte[] ans = new byte[message.length+4];
        ans[0] = 0;
        ans[1] = 9;
        if (delOrAdd == 1)
            ans[2] =(byte)1;
        else
            ans[2]=(byte)0;
        ans[ans.length-1] = 0;
        System.arraycopy(message, 0, ans, 3, message.length);
        availableUsers.forEach((userId, handler) -> {
            connections.send(handler.getId(),ans);
        });
    }
    private void handleDISC() {
        availableUsers.remove(connectionId);
        PacketAck disconnect = new PacketAck((short) 4, (short) 0);
        connections.send(connectionId, disconnect.getPacket());
        connections.disconnect(this.connectionId);
    }
    @Override
    public boolean shouldTerminate() {
return this.shouldTerminate;    }



}







    

