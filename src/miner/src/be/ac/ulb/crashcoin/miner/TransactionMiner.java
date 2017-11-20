package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Est-ce qu'Antoine va stalker mon commit?
 */
public class TransactionMiner {
    
    private final Transaction transaction;
   
    /**
     * Constructor
     * 
     * @param transaction The transaction to mine
     */
    public TransactionMiner(Transaction transaction) {
        this.transaction = transaction;
    }

    /**
     * Mines the transaction until it satisfies the PoW
     * 
     * @return the transaction with the correct nonce
     */
    public Transaction mine() {
        Long currentNonce = 0L;
        byte[] currentHash = null;
        do {
            this.transaction.setNonce(currentNonce);
            try {
                currentHash = this.transaction.hash();
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(TransactionMiner.class.getName()).log(Level.SEVERE,
                        "Unable to mine: no SHA-256 available. Aborting!", ex);
                return null;
            }
            currentNonce += 1;
        } while(!isValid(currentHash));
        return this.transaction;
    }

    /**
     * Checks if a hash satisfies the difficulty
     * 
     * @param hash The hash of a transaction to test
     * @return true if the hash starts with the right amount of null bits and
     * false otherwise
     * @see Parameters.MINING_DIFFICULTY
     */
    private boolean isValid(final byte[] hash) {
        int nbOfNullBytes = Parameters.MINING_DIFFICULTY / Byte.SIZE;
        int nbOfRemaningNullBits = Parameters.MINING_DIFFICULTY - nbOfNullBytes;
        
        if(hash == null)
            return false;
        
        // check the first complete bytes
        for(int byteIdx = 0; byteIdx < nbOfNullBytes; ++byteIdx)
            if(hash[byteIdx] != 0)
                return false;
        // check the remaining bits one by one
        for(int bit = 0; bit < nbOfRemaningNullBits; ++bit)
            if((hash[nbOfNullBytes] & (1 << Byte.SIZE - 1 - bit)) != 0)
                return false;
        return true;
    }
}
