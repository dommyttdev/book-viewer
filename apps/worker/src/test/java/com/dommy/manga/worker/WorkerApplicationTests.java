package com.dommy.manga.worker;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dommy.manga.worker.config.BookStorageProperties;
import com.dommy.manga.worker.config.ConversionProperties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
		"spring.flyway.enabled=false",
		"spring.main.web-application-type=none",
		"storage.books.root=./build/test-storage",
		"conversion.worker.work-root=./build/test-worker",
		"conversion.worker.concurrency=2",
		"conversion.sevenzip.executable-path=7z",
		"conversion.webp.quality=80",
		"conversion.job.timeout=30m"
})
class WorkerApplicationTests {

	@Autowired
	private BookStorageProperties bookStorageProperties;

	@Autowired
	private ConversionProperties conversionProperties;

	@Test
	void contextLoads() {
	}

	@Test
	void bindsLocalDependencyProperties() {
		assertThat(bookStorageProperties.root().endsWith(Path.of("build", "test-storage"))).isTrue();
		assertThat(conversionProperties.worker().workRoot().endsWith(Path.of("build", "test-worker"))).isTrue();
		assertThat(conversionProperties.worker().concurrency()).isEqualTo(2);
		assertThat(conversionProperties.sevenzip().executablePath().toString()).endsWith("7z");
		assertThat(conversionProperties.webp().quality()).isEqualTo(80);
		assertThat(conversionProperties.job().timeout().toMinutes()).isEqualTo(30);
	}

}
