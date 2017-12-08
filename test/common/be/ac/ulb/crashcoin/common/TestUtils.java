package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.KeyPair;
import java.sql.Timestamp;
import org.json.JSONObject;

public class TestUtils {
    
    private static KeyPair kp;
    
    public static void genKeyPair() {
        if(kp == null) {
            kp = Cryptography.getDsaKeyGen().generateKeyPair();
        }
    }
    
    public static Address createAddress() {
        genKeyPair();
        return new Address(kp.getPublic());
    }

    public static Block createBlock() {
        final Block block = new Block(new byte[]{(byte) 0x00}, 0);
        Transaction transaction;
        do {
            transaction = createRewardTransaction();
        } while (block.add(transaction));
        return block;
    }

    public static BlockChain createBlockchain() {
        final BlockChain newBlockChain = new BlockChain();
        newBlockChain.add(createBlock()); //TODO this does not work (block is not valid)
        return newBlockChain;
    }

    public static Transaction createRewardTransaction() {
        genKeyPair();
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        final Transaction transaction = new Transaction(createAddress(), timestamp);
        transaction.sign(kp.getPrivate());
        return transaction;
    }
    
    public static Transaction alterTransaction(Transaction transaction) {
        JSONObject json = transaction.toJSON();
        json.put("lockTime", System.currentTimeMillis());
        return new Transaction(json);
    }
    
}
