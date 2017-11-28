package be.ac.ulb.crashcoin.common;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        final KeyPair kp = kpg.generateKeyPair();
        final PublicKey pk = kp.getPublic();
        return new Address(pk);
    }
    
    public Block createBlock() {
        Block block = new Block();
        Transaction transaction = null;
        do {
            transaction = createTransaction();
        } while(block.add(transaction));
        return block;
    }
    
    public BlockChain createBlockchain() {
        //TODO fill with relevant data
        return new BlockChain();
    }
    
    public Transaction createTransaction() {
        Timestamp timestamp;
        timestamp = new Timestamp(System.currentTimeMillis());
        Transaction transaction = new Transaction(createAddress(), 20, timestamp);
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
        Transaction transaction = createTransaction();
        Transaction copy = new Transaction(transaction.toJSON());
        assertEquals(transaction, copy);
    }

}
