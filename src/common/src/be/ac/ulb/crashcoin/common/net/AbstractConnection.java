package be.ac.ulb.crashcoin.common.net;

import be.ac.ulb.crashcoin.common.JSONable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
    
    protected AbstractConnection(final String name, final Socket acceptedSock) 
            throws UnsupportedEncodingException, IOException {
        super(name);
        
        _sock = acceptedSock;
        _input = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "UTF-8"));
        _output = new PrintWriter(_sock.getOutputStream(), true);
    }
    
    public void sendData(final JSONable jsonData) {
        _output.write(jsonData.toJSON() + "\n");
        _output.flush();
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                final String readLine = _input.readLine();
                if(readLine == null) {
                    break;
                }
                receiveData(readLine);
            }
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        close();
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
    
    protected abstract void receiveData(final String data);
    
}
