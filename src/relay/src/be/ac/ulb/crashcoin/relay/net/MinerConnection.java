package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import org.json.JSONObject;

/**
 * 
 */
class MinerConnection extends AbstractConnection {
    
    private static HashSet<MinerConnection> allMiner = new HashSet<>();
    
    public MinerConnection(final Socket sock) throws IOException {
        super("MinerConnection", sock);
        allMiner.add(this);
        
        start();
    }

    @Override
    protected void receiveData(String data) {
        // TODO 
    }
    
    @Override
    protected void close() {
        super.close();
        allMiner.remove(this);
    }
    
    public static void sendToAll(final JSONObject data) {
        for(final MinerConnection relay : allMiner) {
            relay.sendData(data);
        }
    }
    
}
