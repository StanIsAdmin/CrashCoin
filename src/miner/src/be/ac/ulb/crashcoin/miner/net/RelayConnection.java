package be.ac.ulb.crashcoin.miner.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection to relay node<br>
 * It's a thread and a singleton
 */
public class RelayConnection extends AbstractReconnectConnection {

    private static RelayConnection instance = null;

    Semaphore mutex = new Semaphore(1);
    // Transactions to mined (make a block)
    private final ArrayList<Transaction> transactionsBuffer;
    private boolean hasNewTransactions = false;
    // Mined blocks containing transaction to not mine anymore
    private final ArrayList<Block> blocksBuffer;
    /** Copy of the last block of the blockchain. */
    private Block lastBlock;
    private boolean hasNewBlocks = false;

    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP,
                Parameters.RELAY_PORT_MINER_LISTENER));
        this.transactionsBuffer = new ArrayList<>();
        this.blocksBuffer = new ArrayList<>();
        start();
    }

    @Override
    protected void receiveData(final JSONable data) {
        System.out.println("[DEBUG] get value from relay: " + data);

        if (data instanceof Transaction) {
            try {
                mutex.acquire();
                hasNewTransactions = true;
                transactionsBuffer.add((Transaction) data);
                mutex.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (data instanceof Block) {
            try {
                mutex.acquire();
                hasNewBlocks = true;
                blocksBuffer.add((Block) data);
                lastBlock = blocksBuffer.get(blocksBuffer.size()-1);
                mutex.release();
            } catch (InterruptedException ex) {
                Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        Boolean res = null;
        try {
            mutex.acquire();
            res = this.transactionsBuffer.isEmpty();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    /**
     *
     * @return the list of pending transactions
     */
    public ArrayList<Transaction> getTransactions() {
        ArrayList<Transaction> tmp = new ArrayList<>();
        try {
            mutex.acquire();
            this.hasNewTransactions = false;
            tmp = this.transactionsBuffer;
            this.blocksBuffer.clear();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmp;
    }

    /**
     *
     * @return true if the connection has pending blocks and false otherwise
     */
    public boolean hasBlocks() {
        Boolean res = null;
        try {
            mutex.acquire();
            res = this.blocksBuffer.isEmpty();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    /**
     *
     * @return the list of pending blocks
     */
    public ArrayList<Block> getBlocks() {
        ArrayList<Block> tmp = new ArrayList<>();
        try {
            mutex.acquire();
            this.hasNewBlocks = false;
            tmp = this.blocksBuffer;
            this.blocksBuffer.clear();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmp;
    }

    /**
     * Clears the buffer of pending transactions.
     *
     * @throws InternalError if the the relay has sent unfetched transactions
     */
    public void clearBuffer() throws InternalError {
        try {
            mutex.acquire();
            if (this.hasNewTransactions) {
                throw new InternalError("Unable to clear the transactions buffer: "
                        + "new transactions have been received since last fetch!");
            }
            this.transactionsBuffer.clear();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gives the hash of the last byte in the blockchain.
     * 
     * @return the hash of the last block
     */
    public byte[] getLastBlockOfBlockChainHash()  {
        return Cryptography.hashBytes(lastBlock.headerToBytes());
    }
}
