
package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.IOException;
import java.net.Socket;

/**
 * 
 */
class MinerConnection extends AbstractConnection {

    public MinerConnection(Socket sock) throws IOException {
        super("Miner", sock);
    }

    @Override
    protected void reciveData(String data) {
        // TODO 
    }

}
