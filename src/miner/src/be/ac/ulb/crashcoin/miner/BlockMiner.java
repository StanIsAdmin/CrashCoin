package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import java.security.NoSuchAlgorithmException;

public final class BlockMiner {

    private Block block = null;

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

    public void setBlockToMine(final Block block) {
        this.block = block;
    }

    /**
     * Mines the block until it satisfies the PoW
     *
     * @return the block with the correct nonce
     *
     * @throws java.security.NoSuchAlgorithmException if unable to mine
     */
    public Block mine() throws NoSuchAlgorithmException {
        Integer currentNonce = 0;
        do {
            this.block.setNonce(currentNonce++);
        } while (this.block.isHashValid());
        return this.block;
    }
}
