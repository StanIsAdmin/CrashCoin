package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.KeyPair;
import java.util.ArrayList;
import org.json.JSONObject;

public class TestUtils {
    
    private static final KeyPair kp = Cryptography.getDsaKeyGen().generateKeyPair();
    
    private static final JSONObject secondBlockJSON = new JSONObject("{\"difficulty\":3,\"previousBlock\":\"aCE52mWa+B03Y0FrpwncSef43RcKjPKxrh95miVUslg=\",\"merkleRoot\":\"x3g4IBAKlYluYtskjfk/HHCieG6uaqiyoOQRkEVne0g=\",\"type\":\"Block\",\"nonce\":28,\"listTransactions\":[{\"lockTime\":1512901875251,\"isReward\":false,\"signature\":\"MC0CFFByWiXqamS/1YbHlJwoHHoeCD9uAhUAi1KhdGrKwBCyIXWTetiAaO8dq5Y=\",\"inputs\":[{\"previousOutputAmount\":100,\"type\":\"TransactionInput\",\"previousOutputHash\":\"U3fPKLYZEF0JcM6RxqfSUeVcKxaxxckXJ1AfXxyMGTQ=\"}],\"transactionOutput\":{\"amount\":10,\"address\":{\"type\":\"Address\",\"key\":\"MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAPdJ6xA1xfMGLpaUggbGgo9i8JpbkIKLQdObMGti0QPcYrcfozafTTVbntVQALC1OFQYRjmj1dStSp7g791oPff+nycNHQOHkVPLhJ9m6qPC/ss9kK2ZEdTx7dMbJuDFqnrPfKXnx4yzQFuGsthLn03m4/KvzUsJoBaAb3yLg5pH\"},\"type\":\"TransactionOutput\"},\"changeOutput\":{\"amount\":90,\"address\":{\"type\":\"Address\",\"key\":\"MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAN56JFWbbIUzlf2ycnI8nl52FZ/T8PFB8+CS72hNK4vxAywsyxbAPm6pguSMN8TUAq26fXRU5nfz3KxdcX3YjV1K8KhHoWBDIS67k1Oz2Y+s/4M72ZEdweR0xxxE01bNxpsDCV/vu1A4mZmRVQNOL5ago0qEwwxGBfYi8HMF7rHA\"},\"type\":\"TransactionOutput\"},\"type\":\"Transaction\"},{\"lockTime\":1512901879491,\"isReward\":true,\"transactionOutput\":{\"amount\":100,\"address\":{\"type\":\"Address\",\"key\":\"MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGARBPDa3xhiijtOP/JiVDoM0MWql4p4HLQNxZvCBpoqLd66BHnF3Zc3UW/9JxSyyJn0DMIN31LjHwIMFYRnduGFyUD8vjOtbnLVxNr+ip0wxCFM+VI7G+Bpmn6jgKR0y03T8XMmJt21jiHvnN1Lew2E5xCf2A1ZsbOb9IZquIHSf0=\"},\"type\":\"TransactionOutput\"},\"type\":\"Transaction\"}]}");
    
    public static Address createAddress() {
        return new Address(kp.getPublic());
    }

    public static BlockChain createBlockchain() {
        final BlockChain newBlockChain = new BlockChain();
        return newBlockChain;
    }
    
    public static Block createValidSecondBlock() {
        return new Block(secondBlockJSON);
    }
    
    public static Block createInvalidSecondBlock() {
        final Block block = new Block(new byte[]{(byte) 0x00}, 0);
        Transaction transaction;
        do {
            transaction = createRewardTransaction();
        } while (block.add(transaction));
        return block;
    }
    
    public static Transaction createTransaction() {
        //TODO put actual inputs, and a non-zero amount
        final Address senderAddress = new Address(kp.getPublic());
        final TransactionOutput output = new TransactionOutput(senderAddress, 10);
        final ArrayList<TransactionOutput> inputs = new ArrayList<>();
        inputs.add(output);
        final Transaction transaction = new Transaction(senderAddress, createAddress(), 10, inputs);
        transaction.sign(kp.getPrivate());
        return transaction;
    }

    public static Transaction createRewardTransaction() {
        final Transaction transaction = new Transaction(createAddress());
        return transaction;
    }
    
    public static Transaction alterTransaction(final Transaction transaction) {
        final JSONObject json = transaction.toJSON();
        json.put("lockTime", System.currentTimeMillis());
        return new Transaction(json);
    }
    
}
