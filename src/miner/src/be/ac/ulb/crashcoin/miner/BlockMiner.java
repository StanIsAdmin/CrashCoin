package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public final class BlockMiner {

    private Block block = null;
    private Integer currentNonce = 0;

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
     * @throws java.io.IOException if unable to reach relay
     * @throws be.ac.ulb.crashcoin.miner.AbortMiningException if new block has
     *          arrived during mining
     */
    public Block mineBlock() throws IOException, AbortMiningException {
        currentNonce = 0;
        return mine();
    }
    
    public Block continueMining() throws IOException, AbortMiningException {
        return mine();
    }
    
    private Block mine() throws IOException, AbortMiningException {
        do {
            if(RelayConnection.getRelayConnection().hasBlocks()) {
                throw new AbortMiningException();
            }
            this.block.setNonce(currentNonce++);
        } while (this.block.isHashValid());
        return this.block;
    }
}
