package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.common.net.TestStrJSONable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;
import org.json.JSONObject;

/**
 * Connection to a Relay
 */
public class RelayConnection extends AbstractConnection {
    
    private static HashSet<RelayConnection> allRelay = new HashSet<>();
    
    protected RelayConnection(final Socket acceptedSock) throws UnsupportedEncodingException, IOException {
        super("relay", acceptedSock);
        allRelay.add(this);
        start();
        
        System.out.println("[DEBUG] send TestStrJONable to Relay");
        TestStrJSONable jsonable = new TestStrJSONable();
        sendData(jsonable.toJSON());
    }
    
    @Override
    protected void receiveData(final String data) {
        System.out.println("[DEBUG] get value from relay: " + data);
        // TODO convert data and read it
    }
    
    @Override
    protected void close() {
        super.close();
        allRelay.remove(this);
    }
    
    
    
    public static void sendToAll(final JSONObject data) {
        for(final RelayConnection relay : allRelay) {
            relay.sendData(data);
        }
    }
    
}
