package be.ac.ulb.crashcoin.miner.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Connection to relay node<br>
 * It's a thread and a singleton
 */
public class RelayConnection extends AbstractReconnectConnection {
    
    private static RelayConnection instance = null;
    
    private final ArrayList<Transaction> transactionsBuffer;
    private boolean hasNewTransactions = false;
    
    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP, Parameters.RELAY_PORT_MINER_LISTENER));
        this.transactionsBuffer = new ArrayList<>();
        start();
    }
    
    @Override
    protected void receiveData(final String data) {
        System.out.println("[DEBUG] get value from relay: " + data);
        // TODO analyse data
        // if a new transaction is received:
        // + set hasNewTransactions to true
        // + add it to transactionsBuffer
    }
    
    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new RelayConnection();
            isConnected = true;
        } catch(IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    /**
     * return the unique instance of the singleton
     * 
     * @return an instance of RelayConnection
     * @throws IOException if unable to get connected to the Relay
     */
    public static RelayConnection getRelayConnection() throws IOException {
        if(instance == null) {
            instance = new RelayConnection();
        }
        return instance;
    }

    /**
     * 
     * @return true if the connection has pending transactions and false otherwise
     */
    public boolean hasTransactions() {
        return this.transactionsBuffer.isEmpty();
    }

    /**
     * 
     * @return the list of pending transactions
     */
    public ArrayList<Transaction> getTransactions() {
        this.hasNewTransactions = false;
        return this.transactionsBuffer;
    }
    
    /**
     * Clears the buffer of pendingtransactions.
     * 
     * @throws InternalError if the the relay has sent unfetched transactions
     */
    public void clearBuffer() throws InternalError {
        if(this.hasNewTransactions)
            throw new InternalError("Unable to clear the transactions buffer: "
                   +  "new transactions have been received since last fetch!");
        this.transactionsBuffer.clear();
    }
    
}
