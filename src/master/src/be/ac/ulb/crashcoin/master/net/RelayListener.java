package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.Parameters;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * Listen all connexion from Relay<br>
 * It's a singleton and a thread
 */
public class RelayListener extends Thread {
    
    private static RelayListener instance = null;
    
    private final ServerSocket _sock;
    
    
    private RelayListener() throws IOException {
        super("RelayListener");
        _sock = new ServerSocket(Parameters.MASTER_PORT_LISTENER);
        
        start();
    }
    
    @Override
    public void run() {
        try {
            while(true){
                new RelayConnection(_sock.accept());
            }
        } catch(IOException e) { 
            // Exception in relay
        }
        
        close();
    }
    
    public void close() {
        try {
            _sock.close();
        } catch (IOException e) {
        }
    }
    
    public static RelayListener getListener() throws IOException {
        if(instance == null) {
            instance = new RelayListener();
        }
        return instance;
    }
    
    
}
