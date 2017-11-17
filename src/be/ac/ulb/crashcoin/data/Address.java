package be.ac.ulb.crashcoin.data;

import java.security.PublicKey;

public class Address {
	
	private PublicKey key;
	
	public Address(PublicKey key) {
		this.key = key;
	}
	
	public byte[] toBytes() {
		return key.getEncoded();
	}
}