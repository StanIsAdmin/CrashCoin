package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;

/**
 * 
 */
public class RelayConnection extends AbstractConnection {
    
    private static HashSet<RelayConnection> allRelay = new HashSet<>();
    
    protected RelayConnection(final Socket acceptedSock) throws UnsupportedEncodingException, IOException {
        _sock = acceptedSock;
        _input = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "UTF-8"));
        _output = new PrintWriter(_sock.getOutputStream(), true);
        allRelay.add(this);
        
        start();
    }
    
    @Override
    protected void reciveData(final String data) {
        // TODO convert data and read it
    }
    
    @Override
    protected void close() {
        super.close();
        allRelay.remove(this);
    }
    
    
    @Override
    protected boolean canCreateNewInstance() {
        return false; // Do not try to reconnect master to node
    }
    
}
