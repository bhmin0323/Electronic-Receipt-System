package ssu.sdj.digital_receipt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalReceiptApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalReceiptApplication.class, args);
    }

}
