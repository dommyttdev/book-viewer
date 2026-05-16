package com.dommy.manga.worker.config;

import java.nio.file.Path;
import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "conversion")
public record ConversionProperties(
		@Valid @NotNull Worker worker,
		@Valid @NotNull Sevenzip sevenzip,
		@Valid @NotNull Webp webp,
		@Valid @NotNull Job job) {

	public record Worker(
			@NotNull Path workRoot,
			@Min(1) @Max(10) int concurrency) {
	}

	public record Sevenzip(@NotNull Path executablePath) {
	}

	public record Webp(@Min(1) @Max(100) int quality) {
	}

	public record Job(@NotNull Duration timeout) {
	}
}
