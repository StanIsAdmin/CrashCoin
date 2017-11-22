package be.ac.ulb.crashcoin.common.net;

import be.ac.ulb.crashcoin.common.JSONable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public abstract class AbstractConnection extends Thread {
    
    protected Socket _sock;
    protected BufferedReader _input;
    protected PrintWriter _output;
    
    public void sendToRelay(final JSONable jsonData) {
        _output.write(jsonData.toJSON() + "\n");
        _output.flush();
    }
    
    @Override
    public void run() {
        try {
            
            while(true) {
                String readLine = _input.readLine();
                if(readLine == null) {
                    break;
                }
                reciveData(readLine);
            }
            
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        close();
        reconnect();
    }
    
    protected void close() {
        try {
            _sock.close();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not close socket: {0}", ex.getMessage());
        }
        
        if(_input != null) {
            _input = null;
        }
        
        if(_output != null) {
            _output = null;
        }
    }
    
    protected void reconnect() {
        boolean isConnected = false;
        while(!isConnected) {
            if(canCreateNewInstance()) {
                isConnected = true;
            }
        }
    }
    
    protected abstract void reciveData(final String data);
    
    /**
     * Use when connection have been lost.  Try to create a new instance of this class to reconnect.
     * 
     * @return True if new class have been instanced. False otherwise
     */
    protected abstract boolean canCreateNewInstance();
    
}
