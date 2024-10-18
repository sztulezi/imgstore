package com.nb.imgstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class EncryptionServiceAesTest {
	private static final byte[] AES_KEY = new byte[] { 17, 82, -87, 61, 77, 95, -54, -115, 26, -54, -41, -107, -28,
			-123, -84, -24, -100, -10, 114, -124, -104, 19, -76, -121, 11, -27, -124, -76, 6, -68, -102, -74 };

	private EncryptionServiceAes encryptionServiceAes;

	@Mock
	private Resource res;

	@BeforeEach
	void setUp() throws Exception {
		when(res.exists()).thenReturn(true);
		when(res.getContentAsByteArray()).thenReturn(AES_KEY);
		encryptionServiceAes = new EncryptionServiceAes(res);
	}

	@Test
	void shouldEncryptStringSuccessfully() {
		// given
		String data = "such secret";
		// when
		String actual = encryptionServiceAes.encryptString(data);
		// then
		assertEquals("9UB+fU0Bnnx1thH5/kOfiw==", actual);
	}

	@Test
	void shouldDecryptStringSuccessfully() {
		// given
		String data = "9UB+fU0Bnnx1thH5/kOfiw==";
		// when
		String actual = encryptionServiceAes.decryptString(data);
		// then
		assertEquals("such secret", actual);
	}

}
