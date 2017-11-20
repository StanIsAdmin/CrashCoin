package be.ac.ulb.crashcoin.common;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;


/**
 * Tests all classes that extend the JSONable class.
 * Tests the conversion to and from JSONObject instances.
 */
public class TestJSONable {
    public Address createAddress() {
        KeyPairGenerator kpg = null;
        try { 
            kpg = KeyPairGenerator.getInstance("DSA"); 
        } catch(NoSuchAlgorithmException e) {
            fail("Could not create key pair generator");
        }
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pk = kp.getPublic();
        return new Address(pk);
    }
    
    public Block createBlock() {
        //TODO fill with relevant data
        return new Block();
    }
    
    public BlockChain createBlockchain() {
        //TODO fill with relevant data
        return new BlockChain();
    }
    
    public Transaction createTransaction() {
        //TODO check relevance of data (need for random amounts ?)
        return new Transaction(createAddress(), createAddress(), 20);
    }
    
    @Test
    public void testAddressJSONConversion() {
        Address address = createAddress();
        Address copy = new Address(address.toJSON());
        assertEquals(address, copy);
    }
    
    @Test
    public void testBlockJSONConversion() {
        Block block = createBlock();
        Block copy = new Block(block.toJSON());
        assertEquals(block, copy);
    }
    
    @Test
    public void testTransactionJSONConversion() {
        Transaction transaction = createTransaction();
        Transaction copy = new Transaction(transaction.toJSON());
        assertEquals(transaction, copy);
    }
}
