package be.ac.ulb.crashcoin.common.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Detobel
 */
public abstract class AbstractListener extends Thread {
    
    protected ServerSocket _sock;
    
    protected AbstractListener(final String name, final ServerSocket sock) {
        super(name);
        _sock = sock;
    }
    
    @Override
    public void run() {
        try {
            while(true){
                createNewConnection(_sock.accept());
            }
        } catch(IOException e) { 
            // Exception in relay
        }
        
        close();
    }
    
    protected void close() {
        try {
            _sock.close();
        } catch (IOException e) {
        }
    }
    
    protected abstract void createNewConnection(Socket sock) throws IOException;
    
}
