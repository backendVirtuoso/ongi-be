package com.ongi.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailScheduler {

    private final JobOperator jobOperator;
    private final Job emailSendJob;

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void sendMorningEmail() {
        runJob("MORNING", "email/morning-email");
    }

    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void sendEveningEmail() {
        runJob("EVENING", "email/evening-email");
    }

    private void runJob(String sendType, String templateName) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("sendType", sendType)
                    .addString("templateName", templateName)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.run(emailSendJob, params);
            log.info("Email batch job started: {}", sendType);
        } catch (Exception e) {
            log.error("Email batch job failed: {}", sendType, e);
        }
    }
}
