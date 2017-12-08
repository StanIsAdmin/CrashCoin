package be.ac.ulb.crashcoin.common;

import org.junit.Assert;
import org.junit.Test;

public class TestTransaction {
    
    @Test
    public void testIsReward() {
        final Transaction transaction =  TestUtils.createRewardTransaction();
        Assert.assertTrue(transaction.isReward());
    }
    
    @Test
    public void testCorrectRewardTransactionVerification() {
        final Transaction transaction = TestUtils.createRewardTransaction();
        Assert.assertTrue(transaction.isValid());
    }
    
    @Test
    public void testNotCorrectRewardTransactionVerification() {
        Transaction transaction = TestUtils.createTransaction();
        transaction = TestUtils.alterTransaction(transaction);
        Assert.assertFalse(transaction.isValid());
    }
    
}
