package bgu.spl.net.impl.tftp;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;
import java.util.function.Supplier;
public class TftpServer {
    private Supplier<TftpProtocol> protocolFactory;
    private Supplier<TftpEncoderDecoder> encdecFactory;
    private int port ;
    private TftpConnections connections;
    private int connectionsId = 0;
     volatile boolean shouldTerminate;



    public TftpServer(int port, Supplier<TftpProtocol> protocolFactory, Supplier<TftpEncoderDecoder> encdecFactory) {
        this.encdecFactory = encdecFactory;
        this.protocolFactory = protocolFactory;
        this.port = port;
        this.connections = new TftpConnections();
        this.shouldTerminate=false;

    }
    public void serve() {
        System.out.println("server open!!!");
        try (ServerSocket serverSock = new ServerSocket(port)) {
            while (!Thread.currentThread().isInterrupted()) {// the thread of the client need to cll that
                Socket clientSock = serverSock.accept();
                TftpProtocol temp = protocolFactory.get();
                temp.start(this.connectionsId , connections);
                BlockingConnectionHandler handler = new BlockingConnectionHandler<>(clientSock, encdecFactory.get(), temp,  connectionsId );
                this.connectionsId++;
                execute(handler);

            }
        }
        catch (IOException ex) {}
        System.out.println("server closed!!!");
    }
    protected void execute(BlockingConnectionHandler handler) {
        new Thread(handler).start();
    }
    public static TftpServer threadPerClient(int port, Supplier<TftpProtocol> protocolFactory, Supplier<TftpEncoderDecoder> encoderDecoderFactory) {
        return new TftpServer(port, protocolFactory, encoderDecoderFactory) {
            protected void execute(BlockingConnectionHandler handler) {
                new Thread(handler).start();
            }
        };
    }
}

