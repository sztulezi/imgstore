package com.nb.imgstore.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class ImageControllerITest {
	private static final byte[] IMAGE1_DATA = loadImageFromClasspath("620x413.jpg");
	private static final byte[] IMAGE2_DATA = loadImageFromClasspath("360x288.png");
	private static final byte[] IMAGE3_DATA = loadImageFromClasspath("6010x4012.jpg");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void uploadImages() throws Exception {
		// given
		MockMultipartFile file1 = new MockMultipartFile("images", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGE1_DATA);
		MockMultipartFile file2 = new MockMultipartFile("images", "img2.png", MediaType.IMAGE_PNG_VALUE, IMAGE2_DATA);
		// when
		mockMvc.perform(multipart("/api/files").file(file1).file(file2))
				.andExpectAll(status().isCreated(),
						content().contentType(MediaType.APPLICATION_JSON),
						jsonPath("$", hasSize(2)),
						jsonPath("$[0]", matchesRegex("[0-9a-z]{8}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{12}\\.jpg")),
						jsonPath("$[1]", matchesRegex("[0-9a-z]{8}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{12}\\.png"))
				);
	}

	@Test
	void uploadAndDownloadImage() throws Exception {
		// upload file
		MockMultipartFile file1 = new MockMultipartFile("images", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGE1_DATA);
		MvcResult result = mockMvc.perform(multipart("/api/files").file(file1))
				.andExpectAll(status().isCreated(),
						content().contentType(MediaType.APPLICATION_JSON),
						jsonPath("$", hasSize(1)),
						jsonPath("$[0]", matchesRegex("[0-9a-z]{8}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{12}\\.jpg"))
				).andReturn();
		List<String> list = readResponse(result, new TypeReference<List<String>>() {
		});

		System.out.println(list);

		// download file
		String fileName = list.get(0);
		byte[] downloadedContent = mockMvc.perform(get("/api/file/{fileName}", fileName))
				.andExpectAll(status().isOk(),
						content().contentType(MediaType.IMAGE_JPEG_VALUE)
				).andReturn().getResponse().getContentAsByteArray();

		assertTrue(downloadedContent.length > 0);
	}

	@Test
	void uploadAndDownloadAllImages() throws Exception {
		// upload file
		MockMultipartFile file1 = new MockMultipartFile("images", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGE1_DATA);
		mockMvc.perform(multipart("/api/files").file(file1))
				.andExpectAll(status().isCreated(),
						content().contentType(MediaType.APPLICATION_JSON),
						jsonPath("$", hasSize(1)),
						jsonPath("$[0]", matchesRegex("[0-9a-z]{8}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{12}\\.jpg"))
				);

		// download zip
		byte[] zipContent = mockMvc.perform(get("/api/files"))
				.andExpectAll(status().isOk(),
						content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
				).andReturn().getResponse().getContentAsByteArray();

		assertTrue(zipContent.length > 0);
	}

	@Test
	void uploadLargeImage() throws Exception {
		// upload file
		MockMultipartFile file1 = new MockMultipartFile("images", "img3.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGE3_DATA);
		mockMvc.perform(multipart("/api/files").file(file1))
				.andExpectAll(status().isCreated(),
						content().contentType(MediaType.APPLICATION_JSON),
						jsonPath("$", hasSize(1)),
						jsonPath("$[0]", matchesRegex("[0-9a-z]{8}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{4}\\-[0-9a-z]{12}\\.jpg"))
				);
	}

	private static byte[] loadImageFromClasspath(String name) {
		try {
			String root = ImageControllerITest.class.getClassLoader().getResource(".").getPath();
			return FileUtils.readFileToByteArray(FileUtils.getFile(root, "images", name));
		} catch (IOException e) {
			throw new IllegalStateException("Loading image failed: " + name, e);
		}
	}

	private <T> T readResponse(MvcResult mvcResult, TypeReference<T> typeReference) throws IOException {
		if (mvcResult == null) {
			throw new NullPointerException("MvcResult is null");
		}
		if (typeReference == null) {
			throw new NullPointerException("Target type is null");
		}
		return objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), typeReference);
	}

}
