package be.ac.ulb.crashcoin.common;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestBlockChain {

    @Test
    public void testBlockChainGenesisCreation() {
        BlockChain bc = new BlockChain();
        assertTrue(bc.size() == 1);
    }
}
