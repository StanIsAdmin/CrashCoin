package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;

/**
 * Singleton class that communicates with Relay to receive the transactions to mine.
 *
 */
public class Miner {
    
    /** unique instance of the class */
    private static Miner instance;
    
    /** connection to the Relay */
    private final RelayConnection connection;
    
    /** list of transactions received from the Relay */
    private final ArrayList<Transaction> transactions;
    
    /**
     * Constructor of Miner.
     * 
     * @throws IOException if unable to connect to relay
     */
    protected Miner() throws IOException {
        this.connection = RelayConnection.getRelayConnection();
        this.transactions = new ArrayList<>();
    }
    
    private void removeAlreadyMinedTransactions() {
        this.transactions.addAll(this.connection.getTransactions());
        ArrayList<Block> blocks = this.connection.getBlocks();
        for (Block block : blocks) {
            for (Transaction transaction : block) {
                if(this.transactions.contains(transaction)) {
                    this.transactions.remove(transaction);
                }
            }
        }
        // TODO remove those currently mined
    }
    
    /**
     * Start to wait for transactions and mine them when received.
     * 
     * @throws InterruptedException if the thread has an error when sleeping
     */
    public void startMining() throws InterruptedException {
        BlockMiner miner = new BlockMiner();
        // TODO: find better than a while True?
        while(true) {
            if(!this.connection.hasTransactions()) {
                sleep(100);
            } else if(this.connection.hasBlocks()) {
                this.removeAlreadyMinedTransactions();
            } else {
                this.transactions.addAll(this.connection.getTransactions());
                if(this.transactions.size() >= Parameters.NB_TRANSACTIONS_PER_BLOCK) {
                    miner.setBlockToMine(createBlock());
                    this.connection.sendData(miner.mine());
                }
            }
        }
    }
    
    /**
     * Get the unique instance of the singleton
     * 
     * @return an instance of Miner
     * @throws IOException if unable to connect to the relay
     */
    public static Miner getInstance() throws IOException {
        if(instance == null)
            instance = new Miner();
        return instance;
    }

    /**
     * Creates a block with the transactions received from the relay.
     * 
     * @return a Block made of the transactions
     */
    private Block createBlock() {
        Block ret = new Block();
        for(int i = 0; i < Parameters.NB_TRANSACTIONS_PER_BLOCK; ++i)
            ret.add(this.transactions.get(i));
        // remove the transactions that have been set into the block
        this.transactions.subList(0, Parameters.NB_TRANSACTIONS_PER_BLOCK-1).clear();
        // TODO: complete the block
        return ret;
    }
}
