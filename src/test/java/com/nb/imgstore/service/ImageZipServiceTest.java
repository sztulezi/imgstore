package com.nb.imgstore.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nb.imgstore.model.Image;

@ExtendWith(MockitoExtension.class)
class ImageZipServiceTest {
	private static final byte[] IMAGE1_DATA = { 1, 2 };
	private static final byte[] IMAGE2_DATA = { 3, 4 };
	private static final byte[] ZIP_DATA_PREFIX = { 80, 75, 3, 4, 20, 0, 8, 8, 8, 0 };

	@InjectMocks
	private ImageZipService imageZipService;

	@Mock
	private EncryptionService encryptionService;

	@Test
	void shouldZipAllSuccessfully() {
		// given
		List<Image> images = List.of(
				Image.builder().id(1L).name("img1.jpg").data(IMAGE1_DATA).build(),
				Image.builder().id(2L).name("img2.jpg").data(IMAGE2_DATA).build()
		);
		when(encryptionService.decrypt(any())).then(a -> a.getArgument(0));
		// when
		byte[] actual = imageZipService.zipAll(images);
		// then
		assertAll(
				() -> assertArrayEquals(ZIP_DATA_PREFIX, Arrays.copyOf(actual, 10)),
				() -> verify(encryptionService).decrypt(IMAGE1_DATA),
				() -> verify(encryptionService).decrypt(IMAGE2_DATA)
		);
	}

	@Test
	void shouldZipAllSkipEmptyList() {
		// given
		List<Image> images = List.of();
		// when
		byte[] actual = imageZipService.zipAll(images);
		// then
		assertAll(
				() -> assertArrayEquals(new byte[0], actual),
				() -> verify(encryptionService, never()).decrypt(any())
		);
	}

}
