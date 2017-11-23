package be.ac.ulb.crashcoin.client.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * 
 */
public class RelayConnection extends AbstractReconnectConnection {
    
    private static RelayConnection instance = null;

    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP, Parameters.RELAY_PORT_WALLET_LISTENER));
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

    @Override
    protected void receiveData(String data) {
        // TODO use data
    }
    
    public static RelayConnection getInstance() throws IOException {
        if(instance == null) {
            instance = new RelayConnection();
        }
        return instance;
    }
    
}
