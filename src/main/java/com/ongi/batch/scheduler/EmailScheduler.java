package com.ongi.batch.scheduler;

import com.ongi.batch.processor.EmailContentProcessor;
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
    private final EmailContentProcessor emailContentProcessor;

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendMorningEmail() {
        emailContentProcessor.configureSendType("MORNING", "email/morning-email");
        runJob("MORNING");
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendEveningEmail() {
        emailContentProcessor.configureSendType("EVENING", "email/evening-email");
        runJob("EVENING");
    }

    private void runJob(String sendType) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("sendType", sendType)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.run(emailSendJob, params);
            log.info("Email batch job started: {}", sendType);
        } catch (Exception e) {
            log.error("Email batch job failed: {}", sendType, e);
        }
    }
}
