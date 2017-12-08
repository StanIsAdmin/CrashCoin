package be.ac.ulb.crashcoin.common;

import org.bouncycastle.util.encoders.Hex;

public class Parameters {

    /**
     * Parameters for cryptographic algorithms
     */
    public static final Integer DSA_KEYS_N_BITS = 1024;

    /**
     * Primitive data type sizes
     */
    public static final Integer INTEGER_N_BYTES = Integer.SIZE / Byte.SIZE;

    /**
     * size of a nonce in bytes
     */
    public static final Integer NONCE_N_BYTES = Long.SIZE / Byte.SIZE;

    /**
     * Wallets directory path
     */
    public static final String WALLETS_PATH = "./wallets/";

    // For AES private key encryption
    /**
     * Secret key derivation iteration
     */
    public static final Integer KEY_DERIVATION_ITERATION = 65536;

    /**
     * Key size (guidelines)
     */
    public static final Integer KEY_SIZE = 128;

    /**
     * Salt size in bytes
     */
    public static final Integer SALT_SIZE = 32;

    /** 
     * For BlockChain genesis
     */
    public static final byte[] MASTER_WALLET_PUBLIC_KEY = Hex.decode(("308201b83082012c06072a8648ce3804013082011f02818"
            + "100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6c"
            + "b9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51"
            + "ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf50"
            + "2818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b44"
            + "9167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f"
            + "1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a0381850002818100de7a24559b6c853395fdb272723"
            + "c9e5e76159fd3f0f141f3e092ef684d2b8bf1032c2ccb16c03e6ea982e48c37c4d402adba7d7454e677f3dcac5d717dd88d5d4"
            + "af0a847a16043212ebb9353b3d98facff833bd9911dc1e474c71c44d356cdc69b03095fefbb503899999155034e2f96a0a34a8"
            + "4c30c4605f622f07305eeb1c0").getBytes());
    
    public static final byte[] GENESIS_SIGNATURE = Hex.decode(("302C02144087A57D7A35CA0C49EF4D374EAEC732A49247700214"
            + "71711F5F28010AC1D41A86291E4399F60964B8D2").getBytes());
    
    public static final Integer GENESIS_MINING_DIFFICULTY = 0;

    // For Block
    /**
     * Size of a block header in bytes.
     *
     * @see Block
     */
    public static final Integer BLOCK_HEADER_SIZE = 84;

    /**
     * Number of transactions required to form a block
     */
    public static final Integer NB_TRANSACTIONS_PER_BLOCK = 10;

    /**
     * Magic number in the beginning of a block.
     */
    public static final int MAGIC_NUMBER = 0xCAFE;  // I <3 0xCAFE <3

    // For Mining
    /**
     * difficulty for mining in bits
     */
    public static final Integer MINING_DIFFICULTY = 10;

    public static final Integer MINING_REWARD = 100;

    public static final String HASH_ALGORITHM = "SHA-256";

    /**
     * hash algorithm is SHA-256, then on 256 bits.
     */
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
