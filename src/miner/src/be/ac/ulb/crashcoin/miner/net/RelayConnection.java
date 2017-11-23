package be.ac.ulb.crashcoin.miner.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * Connection to relay node<br>
 * It's a thread and a singleton
 */
public class RelayConnection extends AbstractReconnectConnection {
    
    private static RelayConnection instance = null;
    
    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP, Parameters.RELAY_PORT_MINER_LISTENER));
        start();
    }
    
    @Override
    protected void reciveData(final String data) {
        // TODO analyse data
    }
    
    
    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new RelayConnection();
            isConnected = true;
        } catch(IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    public static RelayConnection getRelayConnection() throws IOException {
        if(instance == null) {
            instance = new RelayConnection();
        }
        return instance;
    }
    
}
