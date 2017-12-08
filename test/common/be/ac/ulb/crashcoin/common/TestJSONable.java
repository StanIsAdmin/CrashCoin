package be.ac.ulb.crashcoin.common;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
        final Block block = TestUtils.createBlock();
        final Block copy = new Block(block.toJSON());
        assertEquals(block, copy);
    }

    @Test
    public void testTransactionJSONConversion() {
        final Transaction transaction = TestUtils.createTransaction();
        final Transaction copy = new Transaction(transaction.toJSON());
        assertEquals(transaction, copy);
    }

    @Test
    public void testBlockChainJSONConcversion() {
        final BlockChain blockChain = TestUtils.createBlockchain();
        final BlockChain copy = new BlockChain(blockChain.toJSON());
        assertEquals(blockChain, copy);
    }

}
