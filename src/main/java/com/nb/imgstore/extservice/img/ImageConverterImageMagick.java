package com.nb.imgstore.extservice.img;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Adapter for the ImageMagick tool.
 * <p>
 * The location of the tool's directory must be set in the <code>MAGICK</code> environment variable to make this work!
 */
@Slf4j
@Service
class ImageConverterImageMagick implements ImageConverter {
	private static final File TEMPDIR = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "imgstore");
	private static final String EXTENSION = ".img";

	@Value("${app.image-converter.scale.max-dimension:5000}")
	private int maxDim;

	/**
	 * It executes a scaling command on the image, keeping the ratio.<br>
	 * The boundaries by default are 5000x5000.<br>
	 * Scaling does not take effect when the image is smaller than the given boundaries, so that it scales only down.<br>
	 * This scaling down feature is turned on by the <code>^></code> switch.
	 * <p>
	 * Example: <code>$ magick a.jpg –scale 5000x5000^> a.jpg</code>
	 * 
	 * @param file - the file to work on
	 */
	@Override
	public byte[] scale(byte[] sourceImage) {
		return scale(sourceImage, maxDim, maxDim);
	}

	/**
	 * It executes a scaling command on the image, keeping the ratio.<br>
	 * Scaling does not take effect when the image is smaller than the given boundaries, so that it scales only down.<br>
	 * This scaling down feature is turned on by the <code>^></code> switch.
	 * <p>
	 * Example: <code>$ magick a.jpg –scale 400x300^> a.jpg</code>
	 * 
	 * @param file   - the file to work on
	 * @param width  - target width
	 * @param height - target height
	 */
	@Override
	public byte[] scale(byte[] sourceImage, int width, int height) {
		initDirectory();
		String id = UUID.randomUUID().toString();
		File file = FileUtils.getFile(TEMPDIR, id + EXTENSION);
		try {
			writeSourceImage(sourceImage, file);
			downScaleImage(file, width + "x1^>");
			downScaleImage(file, "1x" + height + "^>");
			return readResultImage(file);
		} finally {
			file.delete();
		}
	}

	private void downScaleImage(File file, String geometry) {
		String executable = getExecutablePath();
		String[] command = { escape(executable), escape(file.getName()), "-scale", geometry, escape(file.getName()) };
		log.debug("command: {}", Arrays.toString(command));
		try {
			Process process = new ProcessBuilder().directory(TEMPDIR).redirectErrorStream(true).command(command).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				log.trace(line);
			}
			int exitCode = process.waitFor();
			log.debug("imagemagick exit code: {}", exitCode);
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("Unable to scale image", e);
		}
	}

	private String getExecutablePath() {
		String dir = System.getenv("MAGICK");
		if (dir == null) {
			throw new IllegalStateException("ImageMagick directory must be configured by environment var: MAGICK");
		}
		return FileUtils.getFile(dir, "magick").getAbsolutePath();
	}

	private String escape(String path) {
		return "\"" + path + "\"";
	}

	private void initDirectory() {
		try {
			FileUtils.forceMkdir(TEMPDIR);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to create temp dir for image conversion", e);
		}
	}

	private void writeSourceImage(byte[] sourceImage, File file) {
		try {
			FileUtils.writeByteArrayToFile(file, sourceImage);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to write source image", e);
		}
	}

	private byte[] readResultImage(File file) {
		try {
			return FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read result image", e);
		}
	}

}
