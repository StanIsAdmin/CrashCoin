package be.ac.ulb.crashcoin.common;

import java.nio.ByteBuffer;

public class Transaction {

    private final Address srcAddress;
    private final Address destAddress;
    private final Integer amount;

    /**
     * Constructor for transactions
     * 
     * @param srcAddress
     *            CrashCoin address of the source
     * @param destAddress
     *            CrashCoin address of the destination
     * @param amount
     *            Number of CrashCoins
     */
    public Transaction(Address srcAddress, Address destAddress, Integer amount) {
        this.srcAddress = srcAddress;
        this.destAddress = destAddress;
        this.amount = amount;
    }

    /**
     * Creates a byte representation of a transaction. The attributes of the
     * transaction are converted to byte arrays and then concatenated. A transaction
     * requires a byte representation to be able to pass it to a signature
     * algorithm.
     * 
     * @return Bytes of the transaction
     */
    public byte[] toBytes() {
        byte[] srcAddressBytes = srcAddress.toBytes();
        byte[] destAddressBytes = destAddress.toBytes();
        ByteBuffer buffer = ByteBuffer
                .allocate(srcAddressBytes.length + destAddressBytes.length + Parameters.INTEGER_N_BYTES);
        buffer.putInt(amount);
        buffer.put(srcAddressBytes);
        buffer.put(destAddressBytes);
        return buffer.array();
    }

    /** String representation of a transaction */
    public String toString() {
        return "src: " + srcAddress + " | dest: " + destAddress + " | amount: " + amount;
    }
}
