package com.nb.imgstore.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
class EncryptionServiceAes implements EncryptionService {
	private static final String ALGORITHM = "AES"; // TODO switch to a more secure mode
	private final Key key;

	EncryptionServiceAes(@Value("classpath:/cipher/secret.key") Resource res) throws IOException {
		if (res == null || !res.exists()) {
			throw new IllegalStateException("/cipher/secret.key does not exist");
		}
		this.key = new SecretKeySpec(res.getContentAsByteArray(), ALGORITHM);
	}

	@Override
	public byte[] encrypt(byte[] data) {
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			return c.doFinal(data);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalStateException("Encryption failed", e);
		}
	}

	@Override
	public byte[] decrypt(byte[] encryptedData) {
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			return c.doFinal(encryptedData);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalStateException("Decryption failed", e);
		}
	}

	public String encryptString(String data) {
		byte[] encrypted = encrypt(data.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encrypted);
	}

	public String decryptString(String encryptedData) {
		byte[] encrypted = Base64.getDecoder().decode(encryptedData);
		byte[] decrypted = decrypt(encrypted);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

}
