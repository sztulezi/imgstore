package com.nb.imgstore.service;

public interface EncryptionService {

	byte[] encrypt(byte[] data);

	byte[] decrypt(byte[] encryptedData);

}
