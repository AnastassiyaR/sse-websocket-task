package com.example.demo.config;

import com.example.demo.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SseScheduler {

    private final SseService sseService;


    @Scheduled(fixedRate = 5000)
    public void sendPeriodicUpdates() {
        log.info("Sending periodic time update to {} clients",
                sseService.getActiveEmittersCount());

        sseService.sendTimeUpdate();
    }
}
