package be.ac.ulb.crashcoin.miner;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;


public class TransactionMinerTest {
    
    @Test
    public void testIsValidDifficultyTooBig() {
        byte[] array = new byte[1];
        // no Transaction is needed here
        TransactionMiner transactionMiner = new TransactionMiner(null);
        assertFalse(transactionMiner.isValid(array, 10));
    }
    
    @Test
    public void testIsValidIsTrue() {
        byte[] array = {0b01001100};
        // no Transaction is needed here
        TransactionMiner transactionMiner = new TransactionMiner(null);
        assertTrue(transactionMiner.isValid(array, 1));
    }
    
    @Test
    public void testIsValidIsFalse() {
        byte[] array = {0b01001100};
        // no Transaction is needed here
        TransactionMiner transactionMiner = new TransactionMiner(null);
        assertFalse(transactionMiner.isValid(array, 2));
    }
    
    @Test
    public void testMasks() {
        // 0b11111111 is not in the array because otherwise the whole byte would
        // be checked to be different from 0, which is not the point of
        // TransactionMiner.MASKS
        byte[] a = {
            (byte)0b00000000,
            (byte)0b10000000,
            (byte)0b11000000,
            (byte)0b11100000,
            (byte)0b11110000,
            (byte)0b11111000,
            (byte)0b11111100,
            (byte)0b11111110
        };
        assertTrue(Arrays.equals(a, TransactionMiner.MASKS));
    }
}
