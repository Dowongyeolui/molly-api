package org.example.mollyapi;

import org.apache.poi.util.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@EnableRetry
public class MollyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MollyApiApplication.class, args);
        IOUtils.setByteArrayMaxOverride(1073741824);
    }

}
