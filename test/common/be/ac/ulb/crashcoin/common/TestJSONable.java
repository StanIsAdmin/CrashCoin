package be.ac.ulb.crashcoin.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests all classes that extend the JSONable class. Tests the conversion to and
 * from JSONObject instances.
 */
public class TestJSONable {

    @Test
    public void testAddressJSONConversion() {
        final Address address = TestUtils.createAddress();
        final Address copy = new Address(address.toJSON());
        assertEquals(address, copy);
    }

    @Test
    public void testBlockJSONConversion() {
        final Block block = TestUtils.createValidSecondBlock();
        final Block copy = new Block(block.toJSON());
        assertEquals(block, copy);
    }

    @Test
    public void testTransactionJSONConversion() {
        final Transaction transaction = TestUtils.createRewardTransaction();
        final Transaction copy = new Transaction(transaction.toJSON());
        assertEquals(transaction, copy);
    }

    @Test
    public void testBlockChainJSONConcversion() {
        final BlockChain blockChain = TestUtils.createBlockchain();
        blockChain.add(TestUtils.createValidSecondBlock());
        final BlockChain copy = new BlockChain(blockChain.toJSON());
        assertEquals(blockChain, copy);
    }

}
