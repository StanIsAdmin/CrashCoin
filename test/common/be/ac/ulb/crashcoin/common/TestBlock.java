package be.ac.ulb.crashcoin.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestBlock {

    private Block createBlock() {
        return new Block(null, 0);
    }

    @Test
    public void testIsValidDifficultyTooBig() {
        final byte[] array = new byte[1];
        final Block block = createBlock();
        assertFalse(block.isHashValid(array, 10));
    }

    @Test
    public void testIsValidIsTrue() {
        final byte[] array = {0b01001100};
        final Block block = createBlock();
        assertTrue(block.isHashValid(array, 1));
    }

    @Test
    public void testIsValidIsFalse() {
        final byte[] array = {0b01001100};
        final Block block = createBlock();
        assertFalse(block.isHashValid(array, 2));
    }
}
