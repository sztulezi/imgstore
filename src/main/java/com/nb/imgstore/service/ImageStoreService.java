package com.nb.imgstore.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nb.imgstore.exception.InvalidImageException;
import com.nb.imgstore.exception.NotFoundException;
import com.nb.imgstore.extservice.img.ImageConverter;
import com.nb.imgstore.model.Image;
import com.nb.imgstore.repo.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStoreService {
	private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");
	private final EncryptionService encryptionService;
	private final ImageConverter imageConverter;
	private final ImageZipService imageZipService;
	private final ImageRepository imageRepository;

	/**
	 * Returns an image file content by its generated (UUID) name.
	 * 
	 * @param fileName
	 * @return the image file content
	 */
	@Transactional
	public byte[] download(String fileName) {
		return imageRepository.findByName(fileName)
				.map(Image::getData)
				.map(encryptionService::decrypt)
				.orElseThrow(() -> new NotFoundException("Image not found: " + fileName));
	}

	/**
	 * Returns a zip file content of all the images.
	 * 
	 * @return zip file content
	 */
	@Transactional
	public byte[] downloadAll() {
		List<Image> images = imageRepository.findAll();
		return imageZipService.zipAll(images);
	}

	/**
	 * Uploads images by renaming them to a generated (UUID) name.
	 * <p>
	 * It shrinks large images and encrypts them before persisting.
	 * 
	 * @param files
	 * @return the generated image names
	 */
	public List<String> upload(MultipartFile[] files) {
		return Arrays.stream(files)
				.map(file -> toImage(file))
				.map(this::saveImage)
				.toList();
	}

	private Image toImage(MultipartFile imageFile) {
		validateFile(imageFile);
		String fileName = generateFileName(imageFile);
		byte[] content = getImageContent(imageFile);
		byte[] scaledContent = imageConverter.scale(content);
		return Image.builder()
				.name(fileName)
				.type(imageFile.getContentType())
				.data(encryptionService.encrypt(scaledContent))
				.build();
	}

	private void validateFile(MultipartFile imageFile) {
		String type = imageFile.getContentType();
		if (!ALLOWED_TYPES.contains(type)) {
			log.debug("Wrong file type: {} | allowed types: {}", type, ALLOWED_TYPES);
			throw new InvalidImageException("Only png and jpg allowed: " + imageFile.getOriginalFilename());
		}
	}

	private String generateFileName(MultipartFile imageFile) {
		String fileName = imageFile.getOriginalFilename();
		if (!fileName.contains(".")) {
			throw new InvalidImageException("Missing extension: " + fileName);
		}
		String extension = fileName.substring(fileName.lastIndexOf('.'));
		return UUID.randomUUID() + extension;
	}

	private byte[] getImageContent(MultipartFile imageFile) {
		try {
			return imageFile.getBytes();
		} catch (IOException e) {
			throw new InvalidImageException("Malformed image: " + imageFile.getOriginalFilename(), e);
		}
	}

	@Transactional
	private String saveImage(Image image) {
		Image saved = imageRepository.save(image);
		log.debug("file uploaded successfully: {}", saved.getName());
		return saved.getName();
	}

}
