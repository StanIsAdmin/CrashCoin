package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * @param jsonData
     */
    @Override
    protected void receiveData(final JSONable jsonData) {
        
         if(jsonData instanceof Transaction) {
            // TODO new transaction management
            final Transaction transaction = (Transaction) jsonData;
            // Broadcast to the miners directly connected to the relay.
            MinerConnection.sendToAll(jsonData);
            
            // Relay the transaction to the master.
            try {
                MasterConnection.getMasterConnection().sendData(transaction);
            } catch (IOException ex) {
                Logger.getLogger(MinerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
    }
    
}
