package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.util.ArrayList;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class that communicates with Relay to receive the transactions to
 * mine.
 *
 */
public class Miner {

    /**
     * unique instance of the class
     */
    private static Miner instance;

    /**
     * connection to the Relay
     */
    private final RelayConnection connection;

    /**
     * list of transactions received from the Relay
     */
    private final ArrayList<Transaction> transactions;
    
    /**
     * 
     */
    private final BlockMiner miner;

    /**
     * Constructor of Miner.
     *
     * @throws IOException if unable to connect to relay
     */
    protected Miner() throws IOException {
        this.connection = RelayConnection.getRelayConnection();
        this.transactions = new ArrayList<>();
        this.miner = new BlockMiner();
    }

    private void removeAlreadyMinedTransactions() {
        this.transactions.addAll(this.connection.getTransactions());
        for (final Block block : this.connection.getBlocks()) {
            for (final Transaction transaction : block) {
                if (this.transactions.contains(transaction)) {
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
     * @throws java.security.NoSuchAlgorithmException if unable to mine
     */
    public void startMining() throws InterruptedException, NoSuchAlgorithmException {
        // TODO: find better than a while True?
        while (true) {
            if (!this.connection.hasTransactions()) {
                Thread.sleep(100);
            } else if (this.connection.hasBlocks()) {
                this.removeAlreadyMinedTransactions();
            } else {
                makeBlockAndMineIt();
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
        if (instance == null) {
            instance = new Miner();
        }
        return instance;
    }

    /**
     * Creates a block with the transactions received from the relay.
     *
     * @return a Block made of the transactions
     */
    private Block createBlock() throws IOException, NoSuchAlgorithmException {
        // TODO get difficulty properly
        final Block ret = new Block(RelayConnection.getRelayConnection().getLastBlockOfBlockChainHash(), Main.getDifficulty());
        // Add Parameters.NB_TRANSACTIONS_PER_BLOCK-1 transaction because, the last transaction is for the miner !
        for (int i = 0; i < Parameters.NB_TRANSACTIONS_PER_BLOCK - 1; ++i) {
            ret.add(this.transactions.get(i));
        }
        // TODO Add transaction to pay the miner

        // remove the transactions that have been set into the block
        this.transactions.subList(0, Parameters.NB_TRANSACTIONS_PER_BLOCK - 1).clear();
        return ret;
    }
    
    /**
     * Creates a block from transactions in buffer and mines it.
     * 
     * @throws NoSuchAlgorithmException if unable to create the block because
     * of unability to hash last block
     */
    private void makeBlockAndMineIt() throws NoSuchAlgorithmException {
        this.transactions.addAll(this.connection.getTransactions());
        if (this.transactions.size() >= Parameters.NB_TRANSACTIONS_PER_BLOCK) {
            try {
                miner.setBlockToMine(createBlock());
                this.connection.sendData(miner.mine());
            } catch (IOException ex) {
                Logger.getLogger(Miner.class.getName()).log(Level.SEVERE,
                        "Error when asking for relay connection. Abort.", ex);
            }
        }
    }
}
