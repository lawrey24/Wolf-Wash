
package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "main",
    "controller",
    "service",
    "dao",
    "model"
})
public class WolfWashApplication {

    public static void main(String[] args) {
        SpringApplication.run(WolfWashApplication.class, args);
    }
}
