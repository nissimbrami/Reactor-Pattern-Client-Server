package bgu.spl.net.impl.tftp;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) {

        try {
            // Get the local IP address of the machine the server is running on
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("Local IP Address: " + inetAddress.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        TftpServer server = TftpServer.threadPerClient(7777,()->new TftpProtocol(),()->new TftpEncoderDecoder());
        server.serve();

    }
}
