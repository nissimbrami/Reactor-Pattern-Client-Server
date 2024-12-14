package bgu.spl.net.api;

import bgu.spl.net.impl.tftp.TftpConnections;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.io.IOException;

public interface BidiMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(int connectionId, TftpConnections connections);
    
    void process(T message , BlockingConnectionHandler<byte[]> handl , int i) throws IOException;
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
