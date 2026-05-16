package com.dommy.manga.api.config;

import java.nio.file.Path;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.books")
public record BookStorageProperties(
		@NotNull Path root,
		@NotBlank String originalsDir) {
}
