package bgu.spl.net.srv;

import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {
    private TftpProtocol protocol;
    private TftpEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private String name ;
    private volatile boolean connected = true;
    private final int id  ;


    public BlockingConnectionHandler(Socket sock, TftpEncoderDecoder reader, TftpProtocol protocol , int id ) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.id = id ;
    }
    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            int opcode = 0 ;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                byte[] rr = encdec.decodeNextByte((byte) read);
                opcode = encdec.getOpCode();
                //System.out.println("opCode in blockingConnectionHandler is: " + opcode);
                if (rr != null || opcode == 6 || opcode == 10) {
                    protocol.process(rr, this , opcode);
                }

                //encdec = new TftpEncoderDecoder();
                //System.out.println("the encdec reseted");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public int getId(){
        return this.id;
    }
    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }
    public void setUserName(String s){
        this.name = s ;
    }
    public String getUserName(){
        return this.name ;
    }

    @Override
    public void send(byte[] msg) {
        try{
            if(msg != null){
                out.write(encdec.encode(msg));
                out.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
