package be.ac.ulb.crashcoin.common;

import org.junit.Assert;

public class TestTransaction {
    
    //@Test
    public void testCorrectTransactionVerification() {
        final Transaction transaction = TestUtils.createTransaction();
        Assert.assertTrue(transaction.isValid());
    }
    
    //@Test
    public void testNotCorrectTransactionVerification() {
        Transaction transaction = TestUtils.createTransaction();
        transaction = TestUtils.alterTransaction(transaction);
        Assert.assertFalse(transaction.isValid());
    }
    
}
