package com.dommy.manga.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class WorkerStartupLogger implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(WorkerStartupLogger.class);

	@Override
	public void run(ApplicationArguments args) {
		log.info("manga-worker started. Waiting for conversion jobs is not enabled in Sprint S0 minimal setup.");
	}

}
