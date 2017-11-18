package be.ac.ulb.crashcoin.client;

public class WalletInformation implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private byte[] salt;
	private byte[] iv;
	private byte[] encryptedPrivateKey;
	private byte[] publicKey;	
	
	public WalletInformation(byte[] salt, byte[] iv, byte[] encryptedPrivateKey, byte[] publicKey) {
		this.salt = salt;
		this.iv = iv;
		this.encryptedPrivateKey = encryptedPrivateKey;
		this.publicKey = publicKey;
	}
	
	public byte[] getSalt() {
		return(this.salt);
	}
	
	public byte[] getIv() {
		return(this.iv);
	}
	
	public byte[] getEncryptedPrivateKey() {
		return(this.encryptedPrivateKey);
	}
	
	public byte[] getPublicKey() {
		return(this.publicKey);
	}
}
