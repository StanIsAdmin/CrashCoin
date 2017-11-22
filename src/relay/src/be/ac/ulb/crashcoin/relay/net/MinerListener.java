package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 */
public class MinerListener extends AbstractListener {
    
    private static MinerListener instance = null;
    
    private MinerListener() throws IOException {
        super("MinerListener", new ServerSocket(Parameters.RELAY_PORT_LISTENER));
        
        start();
    }
    
    @Override
    protected void createNewConnection(Socket sock) throws IOException {
        new MinerConnection(sock);
    }
    
    public static MinerListener getListener() throws IOException {
        if(instance == null) {
            instance = new MinerListener();
        }
        return instance;
    }
    
    
}
