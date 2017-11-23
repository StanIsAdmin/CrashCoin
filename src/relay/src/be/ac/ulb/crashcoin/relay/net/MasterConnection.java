package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection to master node<br>
 * It's a thread and a singleton
 */
public class MasterConnection extends AbstractReconnectConnection {
    
    private static MasterConnection instance = null;
    
    private MasterConnection() throws UnsupportedEncodingException, IOException {
        super("master", new Socket(Parameters.MASTER_IP, Parameters.MASTER_PORT_LISTENER));
        start();
    }
    
    @Override
    protected void reciveData(final String data) {
        System.out.println("[DEBUG] get value from master: " + data);
        // TODO analyse data
    }
    
    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new MasterConnection();
            isConnected = true;
        } catch(IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    public static MasterConnection getMasterConnection() throws IOException {
        if(instance == null) {
            Logger.getLogger(MasterConnection.class.getName()).log(Level.INFO, "Test new connection to master");
            instance = new MasterConnection();
        }
        return instance;
    }
    
}
