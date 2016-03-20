package com.daggerok.spring.batch;

import com.daggerok.spring.batch.cfg.Cfg;
import com.daggerok.spring.batch.util.Sleep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Import(Cfg.class)
@SpringBootApplication
public class SpringBootBatchApplication {

    @Autowired
    ConfigurableApplicationContext app;

    @PostConstruct
    public void exitAfterTenSeconds() {
        new Thread(() -> {
            Sleep.seconds(10);
            System.exit(SpringApplication.exit(app));
        }).start();
    }

    public static void main(String[] args) {

        SpringApplication.run(SpringBootBatchApplication.class, args);
    }
}
