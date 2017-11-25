package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.IOException;
import java.net.Socket;

/**
 * 
 */
public class WalletConnection extends AbstractConnection {
    
    public WalletConnection(final Socket sock) throws IOException {
        super("WalletConnection", sock);
        start();
    }
    
    /**
     * Simply broadcast the transaction to every connected miner
     * @param data
     */
    @Override
    protected void receiveData(final JSONable data) {
        MinerConnection.sendToAll(data);
    }
    
}
