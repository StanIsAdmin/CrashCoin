package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection to master node<br>
 * It's a thread and a singleton
 */
public class MasterConnection extends AbstractConnection {
    
    private static MasterConnection instance = null;
    
    private MasterConnection() throws UnsupportedEncodingException, IOException {
        _sock = new Socket(Parameters.MASTER_IP, Parameters.MASTER_PORT_LISTENER);
        _input = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "UTF-8"));
        _output = new PrintWriter(_sock.getOutputStream(), true);

        start();
    }
    
    @Override
    public void run() {
        try{
            while(true) {
                String data = _input.readLine();
                reciveData(data);
            }
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        close();
        reconnect();
    }
    
    @Override
    protected void reciveData(final String data) {
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
            instance = new MasterConnection();
        }
        return instance;
    }
    
}
