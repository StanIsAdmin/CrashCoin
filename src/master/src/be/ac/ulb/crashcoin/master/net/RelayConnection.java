package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.JSONable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class RelayConnection extends Thread {
    
    private static HashSet<RelayConnection> allRelay = new HashSet<>();
    
    private final Socket _sock;
    private BufferedReader _input;
    private PrintWriter _output;
    
    
    protected RelayConnection(final Socket acceptedSock) throws UnsupportedEncodingException, IOException {
        this._sock = acceptedSock;
        _input = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "UTF-8"));
        _output = new PrintWriter(_sock.getOutputStream(), true);
        allRelay.add(this);
        
        start();
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
            
        } catch (IOException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        close();
    }
    
    public void sendToRelay(final JSONable jsonData) {
        _output.write(jsonData.toJSON() + "\n");
        _output.flush();
    }

    
    private void reciveData(final String data) {
        // TODO convert data and read it
    }
    
    private void close() {
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
        
        allRelay.remove(this);
    }
    
}
