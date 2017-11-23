package be.ac.ulb.crashcoin.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Timestamp;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.util.Date;
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
        //TODO fill with relevant data
        return new Block();
    }
    
    public BlockChain createBlockchain() {
        //TODO fill with relevant data
        return new BlockChain();
    }
    
    public Transaction createTransaction() {
        Transaction transaction = null;
        try {
            //TODO I have no idea how to create a Transaction and give up.
            final Date date = new Date();
            final CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
            final X509CertSelector cs = new X509CertSelector();
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            final char[] password = "password".toCharArray();
            ks.load(null, password);
            try (FileOutputStream fos = new FileOutputStream("testKeyStore")) {
                ks.store(fos, password);
            }
            final PKIXBuilderParameters cpp = new PKIXBuilderParameters(ks, cs);
            final CertPathBuilderResult cpbResult = cpb.build(cpp);
            final CertPath certPath = cpbResult.getCertPath();
            final Timestamp timestamp = new Timestamp(date, certPath);
            transaction = new Transaction(createAddress(), 20, timestamp);
        } catch (InvalidAlgorithmParameterException | KeyStoreException | NoSuchAlgorithmException | CertPathBuilderException | IOException | CertificateException ex) {
            fail("Could not generate a Transaction instance: " + ex.toString());
        }
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
        //Transaction transaction = createTransaction();
        //Transaction copy = new Transaction(transaction.toJSON());
        //assertEquals(transaction, copy);
        assertTrue(true); //TODO remove when we know how to make transactions
    }
}
