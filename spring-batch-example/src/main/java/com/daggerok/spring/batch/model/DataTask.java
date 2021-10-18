package com.daggerok.spring.batch.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DataTask {
    @Schedules({
            @Scheduled(cron = "0 0 * * * MON-FRI"),
            @Scheduled(fixedRate = 3000)
    })
    public void run() {
        log.info("tic-tac: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu h a m:s")));
    }
}
