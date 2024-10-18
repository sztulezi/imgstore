package com.nb.imgstore.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;

import com.nb.imgstore.model.Image;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageZipService {
	private final EncryptionService encryptionService;

	/**
	 * Zips the provided images, generates the zip file as a byte array
	 * 
	 * @param images
	 * @return the zip file content
	 */
	public byte[] zipAll(List<Image> images) {
		if (images.isEmpty()) {
			return new byte[0];
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zipos = new ZipOutputStream(baos)) {
			for (Image image : images) {
				byte[] data = encryptionService.decrypt(image.getData());
				ZipEntry entry = new ZipEntry(image.getName());
				entry.setSize(data.length);
				zipos.putNextEntry(entry);
				zipos.write(data);
				zipos.closeEntry();
			}
			zipos.close();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Compressing files failed", e);
		}
	}

}
