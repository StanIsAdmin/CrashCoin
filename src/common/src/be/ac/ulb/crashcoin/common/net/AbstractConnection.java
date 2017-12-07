package be.ac.ulb.crashcoin.common.net;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Transaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 */
public abstract class AbstractConnection extends Thread {

    protected Socket _sock;
    protected BufferedReader _input;
    protected PrintWriter _output;

    protected AbstractConnection(final String name, final Socket acceptedSock)
            throws UnsupportedEncodingException, IOException {
        super(name);

        _sock = acceptedSock;
        _input = new BufferedReader(new InputStreamReader(_sock.getInputStream(), "UTF-8"));
        _output = new PrintWriter(_sock.getOutputStream(), true);
    }

    public void sendData(final JSONable jsonData) {
        _output.write(jsonData.toJSON() + "\n");
        _output.flush();
    }

    @Override
    public void run() {
        try {
            while (true) {
                final String readLine = _input.readLine();
                if (readLine == null) {
                    break;
                }
                final JSONable resultObject;
                resultObject = getObjectFromJsonObject(new JSONObject(readLine));

                if (resultObject == null) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Recieved unknow JSONObject: {0}", readLine);
                } else {
                    receiveData(resultObject);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        close();
    }

    protected void close() {
        try {
            _sock.close();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not close socket: {0}", ex.getMessage());
        }

        if (_input != null) {
            _input = null;
        }

        if (_output != null) {
            _output = null;
        }
    }

    private JSONable getObjectFromJsonObject(final JSONObject jsonData)  {
        JSONable result = null;
        switch (jsonData.getString("type")) {

            case "Block":
                result = new Block(jsonData);
                break;

            case "BlockChain":
                result = new BlockChain(jsonData);
                break;

            case "Address":
                result = new Address(jsonData);
                break;

            case "Transaction":
                result = new Transaction(jsonData);
                break;

            case "TestStr":
                result = new TestStrJSONable(jsonData);
                break;

            case "Message":
                result = new Message(jsonData);
                break;
        }

        return result;
    }

    protected abstract void receiveData(final JSONable data);

}
