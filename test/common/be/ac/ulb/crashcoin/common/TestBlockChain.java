package be.ac.ulb.crashcoin.common;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestBlockChain {

    @Test
    public void testBlockChainGenesisCreation()  {
        final BlockChain bc = new BlockChain();
        assertTrue(bc.size() == 1);
    }
    
    @Test
    public void testBlockChainBlockVerification() {
        final BlockChain bc = new BlockChain();
        assertTrue(bc.isValidNextBlock(TestUtils.createValidSecondBlock(), Parameters.MINING_DIFFICULTY));
    }
    
    @Test
    public void testBlockChainBlockAdding() {
        final BlockChain bc = new BlockChain();
        bc.add(TestUtils.createValidSecondBlock());
        assertTrue(bc.size() == 2);
    }
}
