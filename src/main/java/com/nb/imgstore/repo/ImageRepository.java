package com.nb.imgstore.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nb.imgstore.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

	Optional<Image> findByName(String name);

}
