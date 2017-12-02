package be.ac.ulb.crashcoin.miner.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.JSONable;
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

    // Transactions to mined (make a block)
    private final ArrayList<Transaction> transactionsBuffer;
    private boolean hasNewTransactions = false;
    // Mined blocks containing transaction to not mine anymore
    private final ArrayList<Block> blocksBuffer;
    private boolean hasNewBlocks = false;

    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP, Parameters.RELAY_PORT_MINER_LISTENER));
        this.transactionsBuffer = new ArrayList<>();
        this.blocksBuffer = new ArrayList<>();
        start();
    }

    @Override
    protected void receiveData(final JSONable data) {
        System.out.println("[DEBUG] get value from relay: " + data);

        if (data instanceof Transaction) {
            hasNewTransactions = true;
            transactionsBuffer.add((Transaction) data);
        } else if (data instanceof Block) {
            hasNewBlocks = true;
            blocksBuffer.add((Block) data);
        }
    }

    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new RelayConnection();
            isConnected = true;
        } catch (IOException ex) {
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
        if (instance == null) {
            instance = new RelayConnection();
        }
        return instance;
    }

    /**
     *
     * @return true if the connection has pending transactions and false
     * otherwise
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
     *
     * @return true if the connection has pending blocks and false otherwise
     */
    public boolean hasBlocks() {
        return this.blocksBuffer.isEmpty();
    }

    /**
     *
     * @return the list of pending blocks
     */
    public ArrayList<Block> getBlocks() {
        // TODO add semaphore
        this.hasNewBlocks = false;
        ArrayList<Block> tmp = this.blocksBuffer;
        this.blocksBuffer.clear();
        return tmp;
    }

    /**
     * Clears the buffer of pending transactions.
     *
     * @throws InternalError if the the relay has sent unfetched transactions
     */
    public void clearBuffer() throws InternalError {
        // TODO add semaphore
        if (this.hasNewTransactions) {
            throw new InternalError("Unable to clear the transactions buffer: "
                    + "new transactions have been received since last fetch!");
        }
        this.transactionsBuffer.clear();
    }

}
