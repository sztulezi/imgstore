package com.nb.imgstore.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nb.imgstore.exception.dto.ApiError;
import com.nb.imgstore.service.ImageStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {
	private final ImageStoreService imageService;

	@Operation(summary = "Upload image(s)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Image(s) uploaded successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input provided", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
			})
	})
	@PostMapping(value = "/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	public List<String> upload(@RequestPart @NotEmpty MultipartFile[] images) {
		log.info("uploading {} image(s)", images.length);
		return imageService.upload(images);
	}

	@Operation(summary = "Download image by fileName")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Image downloaded successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input provided", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
			}),
			@ApiResponse(responseCode = "404", description = "Image not found", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))
			})
	})
	@GetMapping(value = "/file/{fileName}", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	public byte[] downloadByFileName(@PathVariable @NotEmpty String fileName) {
		log.info("downloading image: {}", fileName);
		return imageService.download(fileName);
	}

	@Operation(summary = "Download all images zipped")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Zip file downloaded successfully") })
	@GetMapping(value = "/files", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> downloadAll() {
		log.info("downloading all images zipped");
		byte[] content = imageService.downloadAll();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition", "attachment; filename=all.zip")
				.contentLength(content.length)
				.body(content);
	}

}
