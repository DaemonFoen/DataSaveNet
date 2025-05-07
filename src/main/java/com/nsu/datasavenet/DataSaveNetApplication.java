package com.nsu.datasavenet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.nsu.datasavenet"})
@ConfigurationPropertiesScan
@EnableJpaRepositories
@EnableTransactionManagement
public class DataSaveNetApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSaveNetApplication.class, args);
    }

}
