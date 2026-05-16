package com.dommy.manga.api;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dommy.manga.api.config.BookStorageProperties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
				"org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
				"org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
		"spring.flyway.enabled=false",
		"storage.books.root=./build/test-storage",
		"storage.books.originals-dir=originals"
})
class ApiApplicationTests {

	@Autowired
	private BookStorageProperties bookStorageProperties;

	@Test
	void contextLoads() {
	}

	@Test
	void bindsBookStorageProperties() {
		assertThat(bookStorageProperties.root().endsWith(Path.of("build", "test-storage"))).isTrue();
		assertThat(bookStorageProperties.originalsDir()).isEqualTo("originals");
	}

}
