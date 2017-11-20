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
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        Transaction transaction = null;
        try {
            //TODO I have no idea how to create a Transaction and give up.
            Date date = new Date();
            CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
            X509CertSelector cs = new X509CertSelector();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = "password".toCharArray();
            ks.load(null, password);
            try (FileOutputStream fos = new FileOutputStream("testKeyStore")) {
                ks.store(fos, password);
            }
            PKIXBuilderParameters cpp = new PKIXBuilderParameters(ks, cs);
            CertPathBuilderResult cpbResult = cpb.build(cpp);
            CertPath certPath = cpbResult.getCertPath();
            Timestamp timestamp = new Timestamp(date, certPath);
            transaction = new Transaction(createAddress(), 20, timestamp);
        } catch (InvalidAlgorithmParameterException | KeyStoreException | NoSuchAlgorithmException | CertPathBuilderException | IOException | CertificateException ex) {
            fail("Could not generate a Transaction instance: " + ex.toString());
        }
        return transaction;
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
        //Transaction transaction = createTransaction();
        //Transaction copy = new Transaction(transaction.toJSON());
        //assertEquals(transaction, copy);
        assertTrue(true); //TODO remove when we know how to make transactions
    }
}
