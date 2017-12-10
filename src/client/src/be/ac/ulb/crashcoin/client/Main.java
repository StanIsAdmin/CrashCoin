package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.client.net.RelayConnection;
import be.ac.ulb.crashcoin.common.Parameters;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {
    
    private static String ip;
    private static Integer port;

    public static void main(final String[] argv) throws IOException, InvalidKeySpecException, NoSuchPaddingException, 
            InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, ClassNotFoundException, GeneralSecurityException {
        
        if(argv.length == 2) {
            ip = argv[0];
            port = Integer.parseInt(argv[1]);
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Default ip and port were applied.");
            ip = Parameters.MASTER_IP;
            port = Parameters.MASTER_PORT_LISTENER;
        }
        
        // Init connection with relay
        RelayConnection.getInstance();
        
        final ClientApplication ca = new ClientApplication();

    }
    
    public static String getIP() {
        return ip;
    }
    
    public static Integer getPort() {
        return port;
    }

}
