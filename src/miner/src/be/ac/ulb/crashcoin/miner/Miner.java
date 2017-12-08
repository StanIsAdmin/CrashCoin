package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.util.ArrayList;
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
     * miner of blocks
     */
    private final BlockMiner miner;
    
    /** copy of the block that @see miner is mining */
    private Block currentlyMinedBlock = null;
    
    private boolean currentBlockIsNew = true;

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
    }

    /**
     * Start to wait for transactions and mine them when received.
     *
     * @throws InterruptedException if the thread has an error when sleeping
     */
    public void startMining() throws InterruptedException {
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
     * Creates a block with the transactions received from the relay ans sets it
     * in this.currentlyMinedBlock.
     */
    private void createBlock() throws IOException {
        // only create a new block if there is not a partial block already existing
        if(currentlyMinedBlock == null) {
            currentlyMinedBlock = new Block(RelayConnection.getRelayConnection().getLastBlockOfBlockChainHash(), Parameters.MINING_DIFFICULTY);
        }
        final int nbTransactionsToAdd = Parameters.NB_TRANSACTIONS_PER_BLOCK - currentlyMinedBlock.size();
        if(nbTransactionsToAdd == 0) {
            currentBlockIsNew = false;
            return;
        }
        currentBlockIsNew = true;
        // Add Parameters.NB_TRANSACTIONS_PER_BLOCK-1 transaction because, the last transaction is for the miner !
        for (int i = 0; i < nbTransactionsToAdd - 1; ++i) {
            currentlyMinedBlock.add(this.transactions.get(i));
        }
        currentlyMinedBlock.add(this.selfRewarding());

        // remove the transactions that have been set into the block
        this.transactions.subList(0, Parameters.NB_TRANSACTIONS_PER_BLOCK - 1).clear();
    }
    
    /**
     * Creates a block from transactions in buffer and mines it.
     * 
     * of unability to hash last block
     */
    private void makeBlockAndMineIt()  {
        this.transactions.addAll(this.connection.getTransactions());
        if (this.transactions.size() >= Parameters.NB_TRANSACTIONS_PER_BLOCK) {
            try {
                createBlock();
                miner.setBlockToMine(currentlyMinedBlock);
                // if block is new, start mining from nonce = 0, else keep mining
                // as it was left
                currentlyMinedBlock = currentBlockIsNew ? miner.mineBlock() : miner.continueMining();
                this.connection.sendData(currentlyMinedBlock);
                currentlyMinedBlock = null;
            } catch (IOException ex) {
                Logger.getLogger(Miner.class.getName()).log(Level.SEVERE,
                        "Error when asking for relay connection. Abort.", ex);
            } catch (AbortMiningException ex) {
                if(!updateCurrentBlock())
                    makeBlockAndMineIt();
            }
        }
    }
    
    /**
     * Updates the currently mined block according to newly mined block received
     * from relay.
     * 
     * Called when new block is received during mining.
     * 
     * @return true if block has been updated and false otherwise
     */
    private boolean updateCurrentBlock() {
        boolean hasRemovedBlocks = false;
        ArrayList<Block> newlyMinedBlocks = connection.getBlocks();
        for(final Block newBlock : newlyMinedBlocks) {
            for(final Transaction minedTransaction : newBlock) {
                if(currentlyMinedBlock.contains(minedTransaction)) {
                    currentlyMinedBlock.remove(minedTransaction);
                    hasRemovedBlocks = true;
                }
            }
        }
        // if at least one transaction has been removed from current block
        // then reset the nonce
        if(hasRemovedBlocks)
            this.currentlyMinedBlock.setNonce(0);
        return hasRemovedBlocks;
    }

    private Transaction selfRewarding()  {
        Transaction transaction = new Transaction(Main.getAddress());
        transaction.sign(Main.privateKey());
        return transaction;
    }
}
