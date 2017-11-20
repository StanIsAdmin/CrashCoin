package be.ac.ulb.crashcoin.common;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import org.json.JSONObject;
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
            kpg = KeyPairGenerator.getInstance("RSA"); 
        } catch(NoSuchAlgorithmException e) {
            fail("Could not create key pair generator");
        }
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pk = kp.getPublic();
        return new Address(pk);
    }
    
    @Test
    public void testAddressJSONConversion() {
        Address address1 = createAddress();
        JSONObject json = address1.toJSON();
        Address address2 = new Address(json);
        assertEquals(address1, address2);
    }
}
