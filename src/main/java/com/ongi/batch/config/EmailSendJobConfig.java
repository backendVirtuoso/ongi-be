package com.ongi.batch.config;

import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.batch.processor.EmailContentProcessor;
import com.ongi.batch.writer.KafkaEmailProducer;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.item.ChunkOrientedStep;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@EnableJdbcJobRepository
@RequiredArgsConstructor
public class EmailSendJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final EmailContentProcessor emailContentProcessor;
    private final KafkaEmailProducer kafkaEmailProducer;

    @Bean
    public Job emailSendJob() {
        return new JobBuilder("emailSendJob", jobRepository)
                .start(emailSendStep())
                .build();
    }

    @Bean
    public ChunkOrientedStep<Subscriber, EmailMessageDto> emailSendStep() {
        return new ChunkOrientedStepBuilder<Subscriber, EmailMessageDto>("emailSendStep", jobRepository, 100)
                .transactionManager(transactionManager)
                .reader(activeSubscriberReader())
                .processor(emailContentProcessor)
                .writer(kafkaEmailProducer)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Subscriber> activeSubscriberReader() {
        return new JpaPagingItemReaderBuilder<Subscriber>()
                .name("activeSubscriberReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Subscriber s WHERE s.status = :status AND s.verifiedAt IS NOT NULL")
                .parameterValues(Map.of("status", SubscriberStatus.ACTIVE))
                .pageSize(100)
                .build();
    }
}
