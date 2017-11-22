package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 */
public class WalletListener extends AbstractListener {
    
    private static WalletListener instance = null;
    
    private WalletListener() throws IOException {
        super("WalletListener", new ServerSocket(Parameters.RELAY_PORT_WALLET_LISTENER));
        
        start();
    }
    
    @Override
    protected void createNewConnection(Socket sock) throws IOException {
        new WalletConnection(sock);
    }
    
    public static WalletListener getListener() throws IOException {
        if(instance == null) {
            instance = new WalletListener();
        }
        return instance;
    }
    
}
