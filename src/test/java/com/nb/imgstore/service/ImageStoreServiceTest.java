package com.nb.imgstore.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.nb.imgstore.exception.InvalidImageException;
import com.nb.imgstore.exception.NotFoundException;
import com.nb.imgstore.extservice.img.ImageConverter;
import com.nb.imgstore.model.Image;
import com.nb.imgstore.repo.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageStoreServiceTest {
	private static final byte[] IMAGE1_DATA = { 1, 2 };
	private static final byte[] IMAGE2_DATA = { 3, 4 };
	private static final byte[] ZIP_DATA = { 5, 5 };

	@InjectMocks
	private ImageStoreService imageStoreService;

	@Mock
	private EncryptionService encryptionService;

	@Mock
	private ImageConverter imageConverter;

	@Mock
	private ImageZipService imageZipService;

	@Mock
	private ImageRepository imageRepository;

	@Test
	void shouldDownloadReturnContent() {
		// given
		String fileName = "img.jpg";
		when(imageRepository.findByName(any())).thenReturn(Optional.of(Image.builder().data(IMAGE1_DATA).build()));
		when(encryptionService.decrypt(any())).then(a -> a.getArgument(0));
		// when
		byte[] actual = imageStoreService.download(fileName);
		// then
		assertAll(
				() -> assertArrayEquals(IMAGE1_DATA, actual),
				() -> verify(imageRepository).findByName(fileName),
				() -> verify(encryptionService).decrypt(IMAGE1_DATA)
		);
	}

	@Test
	void shouldDownloadFail_whenImageNotFound() {
		// given
		String fileName = "img.jpg";
		when(imageRepository.findByName(any())).thenReturn(Optional.empty());
		// when
		NotFoundException ex = assertThrows(NotFoundException.class, () -> imageStoreService.download(fileName));
		// then
		assertAll(
				() -> assertEquals("Image not found: " + fileName, ex.getMessage()),
				() -> verify(imageRepository).findByName(fileName),
				() -> verify(encryptionService, never()).decrypt(any())
		);
	}

	@Test
	void shouldDownloadAllReturnZipContent() {
		// given
		List<Image> images = List.of(Image.builder().build());
		when(imageRepository.findAll()).thenReturn(images);
		when(imageZipService.zipAll(any())).thenReturn(ZIP_DATA);
		// when
		byte[] actual = imageStoreService.downloadAll();
		// then
		assertAll(
				() -> assertArrayEquals(ZIP_DATA, actual),
				() -> verify(imageRepository).findAll(),
				() -> verify(imageZipService).zipAll(images)
		);
	}

	@Test
	void shouldUploadSaveImages() {
		// given
		MultipartFile file1 = new MockMultipartFile("images", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGE1_DATA);
		MultipartFile file2 = new MockMultipartFile("images", "img2.png", MediaType.IMAGE_PNG_VALUE, IMAGE2_DATA);
		MultipartFile[] files = { file1, file2 };
		when(imageConverter.scale(any())).then(a -> a.getArgument(0));
		when(encryptionService.encrypt(any())).then(a -> a.getArgument(0));
		when(imageRepository.save(any())).thenReturn(
				Image.builder().name("abcd0123.jpg").build(),
				Image.builder().name("efgh5678.png").build()
		);
		// when
		List<String> actual = imageStoreService.upload(files);
		// then
		assertAll(
				() -> assertEquals(2, actual.size()),
				() -> assertEquals("abcd0123.jpg", actual.get(0)),
				() -> assertEquals("efgh5678.png", actual.get(1)),
				() -> verify(imageConverter).scale(IMAGE1_DATA),
				() -> verify(imageConverter).scale(IMAGE2_DATA),
				() -> verify(encryptionService).encrypt(IMAGE1_DATA),
				() -> verify(encryptionService).encrypt(IMAGE2_DATA),
				() -> verify(imageRepository, times(2)).save(any(Image.class))
		);
	}

	@Test
	void shouldUploadFail_whenWrongContentType() {
		// given
		MultipartFile file1 = new MockMultipartFile("images", "img1.gif", MediaType.IMAGE_GIF_VALUE, IMAGE1_DATA);
		MultipartFile[] files = { file1 };
		// when
		InvalidImageException ex = assertThrows(InvalidImageException.class, () -> imageStoreService.upload(files));
		// then
		assertAll(
				() -> assertEquals("Only png and jpg allowed: img1.gif", ex.getMessage()),
				() -> verify(imageConverter, never()).scale(any()),
				() -> verify(encryptionService, never()).encrypt(any()),
				() -> verify(imageRepository, never()).save(any())
		);
	}

	@Test
	void shouldUploadFail_whenFileWithoutExtension() {
		// given
		MultipartFile file1 = new MockMultipartFile("images", "img1", MediaType.IMAGE_JPEG_VALUE, IMAGE1_DATA);
		MultipartFile[] files = { file1 };
		// when
		InvalidImageException ex = assertThrows(InvalidImageException.class, () -> imageStoreService.upload(files));
		// then
		assertAll(
				() -> assertEquals("Missing extension: img1", ex.getMessage()),
				() -> verify(imageConverter, never()).scale(any()),
				() -> verify(encryptionService, never()).encrypt(any()),
				() -> verify(imageRepository, never()).save(any())
		);
	}

}
