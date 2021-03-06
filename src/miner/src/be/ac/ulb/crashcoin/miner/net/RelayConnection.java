package be.ac.ulb.crashcoin.miner.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import be.ac.ulb.crashcoin.miner.Main;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Connection to relay node<br>
 * It's a thread and a singleton
 */
public class RelayConnection extends AbstractReconnectConnection {

    private static RelayConnection instance = null;

    private final Semaphore mutex = new Semaphore(1);
    // Transactions to mined (make a block)
    private final ArrayList<Transaction> transactionsBuffer;
    private boolean hasNewTransactions = false;
    // Mined blocks containing transaction to not mine anymore
    private final ArrayList<Block> blocksBuffer;
    /** Copy of the last block of the blockchain. */
    private Block lastBlock;
    
    private final HashSet<Transaction> badTransactionsBuffer;

    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Main.getIp(), Main.getPort()));
        this.transactionsBuffer = new ArrayList<>();
        this.blocksBuffer = new ArrayList<>();
        this.badTransactionsBuffer = new HashSet<>();
        start();
    }

    @Override
    protected void receiveData(final JSONable data) {
        if (data instanceof Transaction) {
            try {
                final Transaction transaction = (Transaction) data;
                mutex.acquire();
                hasNewTransactions = true;
                transactionsBuffer.add(transaction);
                mutex.release();
                
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Recieve transaction from relay ({0}):\n{1}", 
                    new Object[]{_ip, transaction.toString()});
            } catch (InterruptedException ex) {
                Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, ex.getMessage());
            }
            
        } else if (data instanceof Block) {
            try {
                final Block block = (Block) data;
                mutex.acquire();
                blocksBuffer.add(block);
                lastBlock = blocksBuffer.get(blocksBuffer.size()-1);
                mutex.release();
                
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Recieve last block from relay ({0}):\n{1}", 
                    new Object[]{_ip, block.toString()});
            } catch (InterruptedException ex) {
                Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, ex.getMessage());
            }
        } else if(data instanceof Message) {
            final Message message = (Message)data;
            switch(message.getRequest()) {
                case Message.TRANSACTIONS_NOT_VALID:
                    handleTransactionsNotValid(message.getOption());
                    break;
                default:
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Recevied message with unknown request: {0}", message.getRequest());
            }
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Get unknowed value from relay ({0}): {1}", 
                new Object[]{_ip, data});
        }
    }
    
    private void handleTransactionsNotValid(final JSONObject option) {
        Logger.getLogger(getClass().getName()).info("Miner recevied bad transactions");
        final JSONArray badTransactions = option.getJSONArray("transactions");
        for(int i = 0; i < badTransactions.length(); ++i) {
            badTransactionsBuffer.add(new Transaction(badTransactions.getJSONObject(i)));
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
        Boolean res = false;
        try {
            mutex.acquire();
            res = !this.transactionsBuffer.isEmpty();
            mutex.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(RelayConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    
    public boolean hasBadTransaction() {
        return !this.badTransactionsBuffer.isEmpty();
    }
    
    public HashSet<Transaction> getBadTransactions() {
        return this.badTransactionsBuffer;
    }

    /**
     *
     * @return the list of pending transactions
     */
    public ArrayList<Transaction> getTransactions() {
        final ArrayList<Transaction> tmp = new ArrayList<>();
        try {
            mutex.acquire();
            this.hasNewTransactions = false;
            tmp.addAll(transactionsBuffer);
            this.transactionsBuffer.clear();
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
        return lastBlock.getHashHeader();
    }
}
