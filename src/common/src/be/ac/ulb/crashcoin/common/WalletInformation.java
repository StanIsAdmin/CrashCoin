package be.ac.ulb.crashcoin.common;

public class WalletInformation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] salt;
    private final byte[] iv;
    private final byte[] encryptedPrivateKey;
    private final byte[] publicKey;

    public WalletInformation(final byte[] salt, final byte[] iv, final byte[] encryptedPrivateKey, final byte[] publicKey) {
        this.salt = salt;
        this.iv = iv;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.publicKey = publicKey;
    }

    public byte[] getSalt() {
        return (this.salt);
    }

    public byte[] getIv() {
        return (this.iv);
    }

    public byte[] getEncryptedPrivateKey() {
        return (this.encryptedPrivateKey);
    }

    public byte[] getPublicKey() {
        return (this.publicKey);
    }
}
