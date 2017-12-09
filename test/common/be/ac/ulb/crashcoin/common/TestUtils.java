package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.KeyPair;
import java.util.ArrayList;
import org.json.JSONObject;

public class TestUtils {
    
    private static final KeyPair kp = Cryptography.getDsaKeyGen().generateKeyPair();
    
    public static Address createAddress() {
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
    
    public static Transaction createTransaction() {
        //TODO put actual inputs, and a non-zero amount
        final BlockChain bc = createBlockchain();
        final Transaction transaction = new Transaction(createAddress(), createAddress(), 0, new ArrayList<TransactionOutput>());
        transaction.sign(kp.getPrivate());
        return transaction;
    }

    public static Transaction createRewardTransaction() {
        final Transaction transaction = new Transaction(createAddress());
        transaction.sign(kp.getPrivate());
        return transaction;
    }
    
    public static Transaction alterTransaction(final Transaction transaction) {
        final JSONObject json = transaction.toJSON();
        json.put("lockTime", System.currentTimeMillis());
        return new Transaction(json);
    }
    
}
