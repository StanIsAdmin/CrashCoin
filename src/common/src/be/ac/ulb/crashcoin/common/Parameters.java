package be.ac.ulb.crashcoin.common;

public class Parameters {
    /** Parameters for cryptographic algorithms */
    public static final Integer DSA_KEYS_N_BITS = 1024;

    /** Primitive data type sizes */
    public static final Integer INTEGER_N_BYTES = Integer.SIZE / Byte.SIZE;
    
    /** size of a nonce in bytes */
    public static final Integer NONCE_N_BYTES = Long.SIZE / Byte.SIZE;
    
    /** Wallets directory path */
    public static final String WALLETS_PATH = "./wallets/";
    
    
    // For AES private key encryption
    
    /** Secret key derivation iteration */
    public static final Integer KEY_DERIVATION_ITERATION = 65536;
    
    /** Key size (guidelines) */
    public static final Integer KEY_SIZE = 128;
    
    /** Salt size in bytes */
    public static final Integer SALT_SIZE = 32;
    
    // For Mining
    
    /** difficulty for mining in bits */
    public static final Integer MINING_DIFFICULTY = 10;
    
}
