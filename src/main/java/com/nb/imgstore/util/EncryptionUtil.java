package com.nb.imgstore.util;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating a simple 256 bit AES ECB secret key.
 * <p>
 * It generates the key into the user.home/imgstore/secret.key file in a binary form.
 * <p>
 * Such a key on a classpath is used by the {@link EncryptionServiceAes}.
 */
@Slf4j
public class EncryptionUtil {

	public static void main(String[] args) throws Exception {
		File file = FileUtils.getFile(FileUtils.getUserDirectoryPath(), "imgstore", "secret.key");
		log.info("file: {}", file);

		Key generatedKey = generateKey();
		log.info("gKey: {}", Arrays.toString(generatedKey.getEncoded()));

		saveKey(generatedKey, file);
		Key loadedKey = loadKey(file);
		log.info("lKey: {}", Arrays.toString(loadedKey.getEncoded()));
	}

	public static Key generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(256);
		return keyGenerator.generateKey();
	}

	public static void saveKey(Key key, File file) throws IOException {
		FileUtils.writeByteArrayToFile(file, key.getEncoded());
	}

	public static Key loadKey(File file) throws IOException {
		byte[] encoded = FileUtils.readFileToByteArray(file);
		return new SecretKeySpec(encoded, "AES");
	}

}
