package com.eureka.forbes.mcd.infrastructure.config;

import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CommonConfig {
    @Autowired
    private EntityManagerFactory emf;

    @Bean
    public JobExecutionListener listener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                System.out.println(jobExecution);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println(jobExecution);
            }

        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setThreadNamePrefix("job_task_pool");
        executor.initialize();
        return executor;
    }

    @Bean
    public ItemReader<OldCustomer> oldCustomerReader() {
        JpaPagingItemReader<OldCustomer> itemReader = new JpaPagingItemReaderBuilder<OldCustomer>()
                .saveState(false)
                .pageSize(100)
                .queryString("select o from OldCustomer o")
                .name("oldCustomerReader")
                .entityManagerFactory(emf)
                .build();
        return itemReader;
    }
}
