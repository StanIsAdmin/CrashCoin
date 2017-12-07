package be.ac.ulb.crashcoin.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestBlock {

    private Block createBlock() {
        return new Block(null, 0);
    }

    @Test
    public void testIsValidDifficultyTooBig() {
        byte[] array = new byte[1];
        Block block = createBlock();
        assertFalse(block.isHashValid(array, 10));
    }

    @Test
    public void testIsValidIsTrue() {
        byte[] array = {0b01001100};
        Block block = createBlock();
        assertTrue(block.isHashValid(array, 1));
    }

    @Test
    public void testIsValidIsFalse() {
        byte[] array = {0b01001100};
        Block block = createBlock();
        assertFalse(block.isHashValid(array, 2));
    }
}
