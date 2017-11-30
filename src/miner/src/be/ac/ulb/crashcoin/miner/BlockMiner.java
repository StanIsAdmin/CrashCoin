package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class BlockMiner {
    
    private Block block = null;
    
    /** array of masks such that MASKS[i] contains the first i bits with 1s and
     * the 8-i last bits with 0s */
    static public final byte[] MASKS;
    
    static {
        MASKS = new byte[8];
        for(int i = 1; i < 8; ++i) {
            MASKS[i] = (byte) ((1 << (Byte.SIZE - i)) | MASKS[i-1]);
        }
    }
   
    /**
     * Constructor
     * 
     * @param block The block to mine
     */
    public BlockMiner(Block block) {
        this();
        setBlockToMine(block);
    }
    
    public BlockMiner() {
    }
    
    public void setBlockToMine(Block block) {
        this.block = block;
    }

    /**
     * Mines the block until it satisfies the PoW
     * 
     * @return the block with the correct nonce
     */
    public Block mine() {
        Integer currentNonce = 0;
        byte[] currentHash;
        do {
            this.block.setNonce(currentNonce);
            try {
                currentHash = Cryptography.hashBytes(this.block.headerToBytes());
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(BlockMiner.class.getName()).log(Level.SEVERE,
                        "Unable to mine: no " + Parameters.HASH_ALGORITHM
                        +" available. Aborting!", ex);
                return null;
            }
            currentNonce += 1;
        } while(!isValid(currentHash));
        return this.block;
    }
    
    /**
     * Checks if a hash satisfies the difficulty
     *
     * @param hash The hash of a transaction to test
     * @return true if the hash starts with the right amount of null bits and
     * false otherwise
     * @see isValid
     */
    public boolean isValid(final byte[] hash) {
        return isValid(hash, Parameters.MINING_DIFFICULTY);
    }

    /**
     * Checks if a hash satisfies the difficulty
     * 
     * @param hash The hash of a transaction to test
     * @param difficulty The number of null bits that are required
     * @return true if the hash starts with the right amount of null bits and
     * false otherwise
     * @see Parameters.MINING_DIFFICULTY
     */
    public boolean isValid(final byte[] hash, Integer difficulty) {
        int nbOfNullBytes = difficulty / Byte.SIZE;
        int nbOfRemaningNullBits = difficulty - nbOfNullBytes;
        
        if(hash == null || difficulty > Byte.SIZE * hash.length)
            return false;
        
        // check the first complete bytes
        for(int byteIdx = 0; byteIdx < nbOfNullBytes; ++byteIdx)
            if(hash[byteIdx] != 0)
                return false;
        // and then check the last remaining bits
        if(nbOfRemaningNullBits > 0 && (hash[nbOfNullBytes] & MASKS[nbOfRemaningNullBits]) != 0)
            return false;
        return true;
    }
}
