package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.client.net.RelayConnection;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {

    public static void main(final String[] args) throws IOException, InvalidKeySpecException, NoSuchPaddingException, 
            InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, ClassNotFoundException, GeneralSecurityException {
        
        // Init connection with relay
        RelayConnection.getInstance();
        
        final ClientApplication ca = new ClientApplication();

    }

}
