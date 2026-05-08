package com.cht.procurementManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProcurementManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProcurementManagementApplication.class, args);
	}

}
