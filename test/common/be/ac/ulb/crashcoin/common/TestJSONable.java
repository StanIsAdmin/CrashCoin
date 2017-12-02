package be.ac.ulb.crashcoin.common;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Tests all classes that extend the JSONable class. Tests the conversion to and
 * from JSONObject instances.
 */
public class TestJSONable {

    public PrivateKey genPrivateKey() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            fail("Could not create key pair generator");
        }
        final KeyPair kp = kpg.generateKeyPair();
        return kp.getPrivate();
    }
    
    public Address createAddress() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            fail("Could not create key pair generator");
        }
        final KeyPair kp = kpg.generateKeyPair();
        final PublicKey pk = kp.getPublic();
        return new Address(pk);
    }

    public Block createBlock() {
        final Block block = new Block(new byte[]{(byte) 0x00}, 0);
        Transaction transaction;
        do {
            transaction = createTransaction();
        } while (block.add(transaction));
        return block;
    }

    public BlockChain createBlockchain() {
        final BlockChain newBlockChain = new BlockChain();
        newBlockChain.add(createBlock()); //TODO this does not work (block is not valid)
        return newBlockChain;
    }

    public Transaction createTransaction() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        final Transaction transaction = new Transaction(createAddress(), 20, timestamp);
        transaction.sign(genPrivateKey());
        return transaction;
    }

    @Test
    public void testAddressJSONConversion() {
        final Address address = createAddress();
        final Address copy = new Address(address.toJSON());
        assertEquals(address, copy);
    }

    @Test
    public void testBlockJSONConversion() {
        final Block block = createBlock();
        final Block copy = new Block(block.toJSON());
        assertEquals(block, copy);
    }

    @Test
    public void testTransactionJSONConversion() {
        final Transaction transaction = createTransaction();
        final Transaction copy = new Transaction(transaction.toJSON());
        assertEquals(transaction, copy);
    }

    @Test
    public void testBlockChainJSONConcversion() {
        final BlockChain blockChain = createBlockchain();
        final BlockChain copy = new BlockChain(blockChain.toJSON());
        assertEquals(blockChain, copy);
    }

}
