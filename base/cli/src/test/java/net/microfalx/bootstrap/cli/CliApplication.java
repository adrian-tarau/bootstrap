package net.microfalx.bootstrap.cli;

import net.microfalx.bootstrap.configuration.annotation.EnableConfigurationMapping;
import net.microfalx.bootstrap.core.utils.BootUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap"})
@EnableConfigurationMapping({"net.microfalx.bootstrap"})
public class CliApplication implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Running CLI with arguments: " + String.join(" ", args));
    }

    public static void main(String[] args) {
        BootUtils.asCli();
        SpringApplication.run(CliApplication.class, args);
    }
}
