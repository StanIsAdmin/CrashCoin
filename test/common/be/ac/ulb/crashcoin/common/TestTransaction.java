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
    public void testRewardTransactionIsValid() {
        final Transaction transaction = TestUtils.createRewardTransaction();
        Assert.assertTrue(transaction.isValidReward());
    }
    
    @Test
    public void testRewardTransactionIsNotValid() {
        Transaction transaction = TestUtils.createTransaction();
        transaction = TestUtils.alterTransaction(transaction);
        Assert.assertFalse(transaction.isValidReward());
    }
    
    @Test
    public void testNotIsReward() {
        final Transaction transaction = TestUtils.createTransaction();
        Assert.assertFalse(transaction.isReward());
    }
    
    @Test
    public void testNotRewardTransactionIsValid() {
        final Transaction transaction = TestUtils.createTransaction();
        Assert.assertTrue(transaction.isValidNonReward());
    }
    
    @Test
    public void testNotRewardTransactionIsNotValid() {
        Transaction transaction = TestUtils.createTransaction();
        transaction = TestUtils.alterTransaction(transaction);
        Assert.assertFalse(transaction.isValidNonReward());
    }
    
}
