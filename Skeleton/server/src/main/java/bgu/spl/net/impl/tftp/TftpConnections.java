package bgu.spl.net.impl.tftp;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpConnections implements Connections<byte[]> {

    public ConcurrentHashMap<Integer, BlockingConnectionHandler<byte[]>> handlers = new ConcurrentHashMap<>();

    public TftpConnections(){
        this.handlers = new ConcurrentHashMap<>();
    }
    public void connect(int connectionId, BlockingConnectionHandler<byte[]> handler) {
            handlers.put(connectionId, handler);
    }
    public boolean send(int connectionId, byte[] msg) {
        //System.out.println("starting to sending the data");
        BlockingConnectionHandler<byte[]> handler = handlers.get(connectionId);
        if (handler != null) {
            handler.send(msg);
            return true;
        }
        return false;
    }

    public void disconnect(int connectionId) {
        BlockingConnectionHandler<byte[]> handler = handlers.remove(connectionId);
        if (handler != null) {
            try {
                handler.close();
            } catch (Exception e) {
                System.out.println("Error when trying to close the connection: " + e.getMessage());
            }
        }
    }
    public boolean isConnected(int id , String s){
        if (handlers.containsKey(id))
            return true;
        else{
            for (BlockingConnectionHandler<byte[]> handler : handlers.values()) {
                if (handler.getUserName() == s)
                    return true ;
            }
        }
        return false;
    }

}
