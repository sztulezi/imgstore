package com.nb.imgstore.extservice.img;

/**
 * Collection of image manipulating functions
 */
public interface ImageConverter {

	/**
	 * Scales down an image into the default (5000x5000) boundaries, keeping the ratio.
	 * 
	 * @param sourceImage
	 * @return scaled down image
	 */
	byte[] scale(byte[] sourceImage);

	/**
	 * Scales down an image into the given boundaries, keeping the ratio.
	 * 
	 * @param sourceImage
	 * @param width
	 * @param height
	 * @return scaled down image
	 */
	byte[] scale(byte[] sourceImage, int width, int height);

}
