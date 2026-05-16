package com.dommy.manga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
class LocalDependencyHealthLogger implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(LocalDependencyHealthLogger.class);

	private final HealthEndpoint healthEndpoint;

	LocalDependencyHealthLogger(HealthEndpoint healthEndpoint) {
		this.healthEndpoint = healthEndpoint;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("manga-worker local dependency health: db={}, elasticsearch={}, rabbit={}",
				statusFor("db"), statusFor("elasticsearch"), statusFor("rabbit"));
	}

	private Object statusFor(String component) {
		HealthDescriptor health = this.healthEndpoint.healthForPath(component);
		return (health != null) ? health.getStatus() : "UNKNOWN";
	}
}
