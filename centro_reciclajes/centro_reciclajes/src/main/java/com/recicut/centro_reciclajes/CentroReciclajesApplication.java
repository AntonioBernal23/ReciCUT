package com.recicut.centro_reciclajes;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDiscoveryClient
public class CentroReciclajesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentroReciclajesApplication.class, args);
	}
}