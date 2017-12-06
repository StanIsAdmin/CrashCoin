package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.JsonUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Wallet {

    private PublicKey publicKey;
    private KeyPairGenerator dsaKeyGen;
    private ArrayList<Transaction> transactionsList;
    private static Wallet instance = null;

    /**
     * Constructs an empty wallet. This constructor behaves differently if one
     * passes a Keypair to it.
     *
     * @throws NoSuchProviderException
     */
    private Wallet() {
        this.publicKey = null;
        try {
            dsaKeyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Error] Could not find DSA key-pair generator");
        } catch (NoSuchProviderException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Constructs a wallet from a key pair. Only the public key is stored. For
     * that reason, the key pair needs to be passed to signTransaction to be
     * able to sign a transaction. After constructing a wallet using this
     * constructor, one cannot generate keys with the same wallet anymore.
     *
     * @param keyPair Pair of keys
     * @throws NoSuchProviderException
     * @see signTransaction
     */
    private Wallet(KeyPair keyPair) {
        this();
        this.publicKey = null;
        this.publicKey = keyPair.getPublic();
    }

    /**
     * Generates a DSA key pair, composed of a public key and a private key. The
     * key size is defined in parameters. This method can be called at most one
     * time per wallet.
     *
     * @return Pair of DSA keys
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public KeyPair generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
        if (publicKey != null) {
            System.out.println("[Error] Only one key pair can be assigned to a wallet");
            return null;
        }
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        dsaKeyGen.initialize(Parameters.DSA_KEYS_N_BITS, random);
        final KeyPair keyPair = dsaKeyGen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        return keyPair;
    }
    
    public void addTransaction(Transaction transaction) {
        transactionsList.add(transaction);
    }

    public ArrayList<Transaction> getTransactions() {
        return transactionsList;
    }

    public Signature dsaFromPrivateKey(final PrivateKey privateKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Error] Could not find DSA signature algorithm");
        } catch (NoSuchProviderException e) {
            System.out.println("[Error] Could not find provider for DSA");
        }

        try {
            // Using private key to sign with DSA
            dsa.initSign(privateKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }

    public Signature dsaFromPublicKey(final PublicKey publicKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e2) {
            e2.printStackTrace();
        }
        try {
            // Using public key to verify signatures
            dsa.initVerify(publicKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }

    /**
     * Returns a transaction signature using DSA algorithm.
     *
     * @param privateKey private key
     * @param bytes data to sign
     * @return transaction signature
     */
    public byte[] signTransaction(final PrivateKey privateKey, final byte[] bytes) {
        final Signature dsa = dsaFromPrivateKey(privateKey);
        byte[] signature = null;
        try {
            // Running DSA
            dsa.update(bytes, 0, bytes.length);
            signature = dsa.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public boolean verifySignature(final PublicKey publicKey, final byte[] transaction, final byte[] signature) {
        final Signature dsa = dsaFromPublicKey(publicKey);

        boolean verified = false;
        try {
            dsa.update(transaction, 0, transaction.length);
            verified = dsa.verify(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return verified;
    }

    /**
     * Get the unique public key
     *
     * @return The public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public Address getAddress() {
        return new Address(publicKey);
    }

    public static Wallet readWalletFile(final File f, final char[] userPassword) throws FileNotFoundException,
            IOException, ClassNotFoundException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, NoSuchProviderException {
        final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        final WalletInformation walletInformation = (WalletInformation) ois.readObject();
        ois.close();

        // Retrieve wallet information stored on disk
        final byte[] salt = walletInformation.getSalt();
        final byte[] iv = walletInformation.getIv();
        final byte[] encryptedPrivateKey = walletInformation.getEncryptedPrivateKey();
        final byte[] publicKeyBytes = walletInformation.getPublicKey();

        final SecretKey decryptionKey = Wallet.computeSecretKey(userPassword, salt);

        // Decrypt the private key with the decryption key generated from the user password
        // Initialize a cipher to the decryption mode with the decryptionKey
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));

        // Decrypt the private key
        try {

            final byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);

            // Create a PairKey with the encoded public and private keys
            final KeyPair keyPair = Wallet.createKeyPairFromEncodedKeys(publicKeyBytes, privateKeyBytes);

            // Create a Wallet object with the KeyPair
            final Wallet wallet = new Wallet(keyPair);

            // Verify the private key generated by the password entered by the user
            if (wallet.verifyPrivateKey(keyPair)) {
                System.out.println("Welcome in your wallet!");
                System.out.println("Your public key:");
                System.out.println(JsonUtils.encodeBytes(keyPair.getPublic().getEncoded()));
                System.out.println("Your private key:");
                System.out.println(JsonUtils.encodeBytes(keyPair.getPrivate().getEncoded()));
                System.out.println("");
                return wallet;

            } else {
                System.out.println("The password you entered is incorrect");
                return null;
            }

        } catch (BadPaddingException e) {

            /*
            * Normally raised by the line:
            *  
            * byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);
            * 
            * In case of a wrong passowrd.
            * 
            * PKCS 5 Padding has a specific structure preventing the decryption
            * to complete with the wrong key (raising a BadPaddingException)
            * It's a sane check but not secure enough. If by chance the wrong
            * password produce the same PKCS5 padding as the right one, no
            * exception will be raised. The safety check can't then rely solely
            * on a bad padding exception.            	 * 
            * 
            * A second test with the public will be performed afterward to ensure
            * that the private key is indeed the right one (see hereunder).
            * 
             */
            System.out.println("The password you entered is incorrect");
            return null;

        }
    }

    public void writeWalletFile(final char[] userPassword, final String accountName) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException,
            IOException {
        final KeyPair keyPair = this.generateKeys();

        final PublicKey publicKey = keyPair.getPublic();
        final PrivateKey privateKey = keyPair.getPrivate();

        // Get the bytes representation of the keys
        final byte[] publicKeyBytes = publicKey.getEncoded();
        final byte[] privateKeyBytes = privateKey.getEncoded();
        
        writeWalletFile(userPassword, accountName, publicKeyBytes, privateKeyBytes);
    }
    
    
    public void writeWalletFile(final char[] userPassword, final String accountName, 
            final byte[] publicKeyBytes, final byte[] privateKeyBytes) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException,
            IOException {

        // Encrypt the private key using AES-128 protocol with the user password
        // Compute a salt to avoid dictionary attacks (to turn a password into a secret key)
        // The salt is not kept secret but is needed for decryption
        final SecureRandom random = new SecureRandom();
        final byte[] salt = new byte[Parameters.SALT_SIZE];
        random.nextBytes(salt);

        final SecretKey encryptionKey = this.computeSecretKey(userPassword, salt);

        // Encrypt the private key with the encryption key generated from the user password
        // Initialize a cipher to the encryption mode with the encryptionKey
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

        // Get the IV necessary to decrypt the message later
        // In CBC mode, each block is XORed with the output of the previous block
        // The IV represents the arbitrary "previous block" to be used for the first block
        final AlgorithmParameters parameters = cipher.getParameters();
        final byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();

        // Encrypt the private key
        final byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);
        // Write wallet information in the wallet file
        final WalletInformation walletInformation = new WalletInformation(salt, iv, encryptedPrivateKey, publicKeyBytes);

        // Creates wallet folder if not exists
        final File walletFolder = new File(Parameters.WALLETS_PATH);
        if (!walletFolder.exists()) {
            walletFolder.mkdir();
        }

        // Creates new wallet file
        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(walletInformation);
        oos.flush();
        oos.close();

        System.out.println("The creation of your wallet completed successfully");
        System.out.println("Please sign in and start crashing coins");
    }
    
    public static Wallet getInstance() {
        if (Wallet.instance == null) {
            Wallet.instance = new Wallet();
        }
        return Wallet.instance;
    }
    
    public static Wallet getInstance(KeyPair keypair){
        if (Wallet.instance == null) {
            Wallet.instance = new Wallet(keypair);
        }
        return Wallet.instance;
    }

    /**
     * Compute an encryption / decryption key (they are the same) from the
     * password and the salt<br>
     *
     * Information: PBKDF2 is a password-based key derivation function Used
     * PBKDF2WithHmacSHA1 instead of PBKDF2WithHmacSHA256 because of some
     * problems if we run the project on java <= 7 (plus the guidelines say 128
     * bits so it's ok)
     *
     * @param userPassword password of user
     * @param salt extended string
     * @return SecretKey
     */
    private static SecretKey computeSecretKey(final char[] userPassword, final byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final KeySpec spec = new PBEKeySpec(userPassword, salt, Parameters.KEY_DERIVATION_ITERATION,
                Parameters.KEY_SIZE);
        final SecretKey tmpKey = factory.generateSecret(spec);
        final SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");

        return (secretKey);
    }

    /**
     * Convert encoded private and public keys (bytes) to Private / PublicKey
     * interfaces and generate a KeyPair from them in order to construct a
     * Wallet object in the signIn method<br>
     * <b>Two different encoding</b>
     *
     * @param publicKeyBytes the public key with encoding X509
     * @param privateKeyBytes the private key with encoding PKCS8
     * @return the key pair
     */
    private static KeyPair createKeyPairFromEncodedKeys(final byte[] publicKeyBytes, final byte[] privateKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Generate specs
        final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        final KeyFactory factory = KeyFactory.getInstance("DSA");

        // Create PublicKey and PrivateKey interfaces using the factory
        final PrivateKey privateKey = factory.generatePrivate(privateKeySpec);
        final PublicKey publicKey = factory.generatePublic(publicKeySpec);

        return (new KeyPair(publicKey, privateKey));

    }

    /**
     * The objective of this method is to verify that the private key that we've
     * just decrypted in the login method using the user password is the valid
     * one. If the user entered a wrong password the decryption cipher would
     * still produce some results, i.e. a wrong private key
     *
     * To solve this, Antoine proposed to create a fake local trasaction with
     * the private key that we've just decrypted and verify it with the public
     * key associated with the user. If the transaction cannot be verified with
     * the public key, then the private key and the password were wrong.
     *
     * @param keyPair the key pair
     * @return True if signature is valid
     */
    public Boolean verifyPrivateKey(final KeyPair keyPair) {
        final Boolean verified;
        final PrivateKey privateKey = keyPair.getPrivate();
        final PublicKey publicKey = keyPair.getPublic();

        // Create dummy transaction
        final byte[] dummyTransaction = new byte[50];
        new Random().nextBytes(dummyTransaction);

        // Sign the dummy transaction with the private key that we want to verify
        final byte[] dummySignature = this.signTransaction(privateKey, dummyTransaction);

        // Verify the signature using the public key and the specific Wallet method
        verified = this.verifySignature(publicKey, dummyTransaction, dummySignature);

        return (verified);
    }

}
