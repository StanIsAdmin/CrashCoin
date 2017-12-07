package be.ac.ulb.crashcoin.client;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {

    public static void main(final String[] args) throws IOException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, ClassNotFoundException {

        final ClientApplication ca = new ClientApplication();

    }

}
