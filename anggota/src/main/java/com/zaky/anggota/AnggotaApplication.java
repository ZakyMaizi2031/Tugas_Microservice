package com.zaky.anggota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AnggotaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnggotaApplication.class, args);
	}

}
