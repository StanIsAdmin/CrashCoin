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
    
    // For Block
    
    /** 
     * Size of a block header in bytes.
     * 
     * @see Block
     */
    public static final Integer BLOCK_HEADER_SIZE = 84;
    
    /** Number of transactions required to form a block */
    public static final Integer NB_TRANSACTIONS_PER_BLOCK = 10;
    
    /** Magic number in the beginning of a block. */
    public static final int MAGIC_NUMBER = 0xCAFE;  // I <3 0xCAFE <3
    
    // For Mining
    
    /** difficulty for mining in bits */
    public static final Integer MINING_DIFFICULTY = 10;
    
    public static final String HASH_ALGORITHM = "SHA-256";
    
    /** hash algorithm is SHA-256, then on 256 bits. */
    public static final Integer NB_BYTES_PER_HASH = 256 / Byte.SIZE;
    
    public static final String MASTER_IP = "127.0.0.1";
    
    public static final Integer MASTER_PORT_LISTENER = 2017;
    
    public static final String RELAY_IP = "127.0.0.1";
    
    /**
     * Specific port which Relay listen for connection from Miner
     */
    public static final Integer RELAY_PORT_MINER_LISTENER = 2018;
    
    /**
     * Specific port which Relay listen for connection from Wallet
     */
    public static final Integer RELAY_PORT_WALLET_LISTENER = 2019;    
}
