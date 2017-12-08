package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import org.json.JSONObject;

public class TestUtils {
    
    public static PrivateKey genPrivateKey() {
        KeyPairGenerator kpg = Cryptography.getDsaKeyGen();
        final KeyPair kp = kpg.generateKeyPair();
        return kp.getPrivate();
    }
    
    public static Address createAddress() {
        KeyPairGenerator kpg = Cryptography.getDsaKeyGen();
        final KeyPair kp = kpg.generateKeyPair();
        final PublicKey pk = kp.getPublic();
        return new Address(pk);
    }

    public static Block createBlock() {
        final Block block = new Block(new byte[]{(byte) 0x00}, 0);
        Transaction transaction;
        do {
            transaction = createTransaction();
        } while (block.add(transaction));
        return block;
    }

    public static BlockChain createBlockchain() {
        final BlockChain newBlockChain = new BlockChain();
        newBlockChain.add(createBlock()); //TODO this does not work (block is not valid)
        return newBlockChain;
    }

    public static Transaction createTransaction() {
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        final Transaction transaction = new Transaction(createAddress(), timestamp);
        transaction.sign(genPrivateKey());
        return transaction;
    }
    
    public static Transaction alterTransaction(Transaction transaction) {
        // Attempt to change the destination of the transaction.
        JSONObject json = transaction.toJSON();
        json.put("destAddress", createAddress());
        return new Transaction(json);
    }
    
}
