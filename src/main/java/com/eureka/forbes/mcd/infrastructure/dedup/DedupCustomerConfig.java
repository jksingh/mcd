package com.eureka.forbes.mcd.infrastructure.dedup;

import com.eureka.forbes.mcd.infrastructure.enitity.Customer;
import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Iterates over legacy customer data and uses DedupProcessor to get the related customers
 * Then uses CustomerWriter to save the related customer in database with batch size of 10.
 * Uses multiple threads.
 */
@Configuration
@Slf4j
@Profile("dedup")
public class DedupCustomerConfig {

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobExecutionListener listener;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ItemReader<? extends OldCustomer> oldCustomerReader;
    @Autowired
    private CustomerWriter customerWriter;
    @Autowired
    private RetryableWrapper retryableWrapper;
    @Autowired
    private DedupProcessor dedupProcessor;

    @Bean
    public ItemWriter<Customer> customerItemWriter() {
        return chunk -> {
            try {
                retryableWrapper.withRetry(() -> customerWriter.saveToDb(chunk));
            } catch (Exception e) {
                log.warn("got exception with writing", e);
            }
        };
    }

    @Bean
    public Job dedupCustomerJob() {
        return new JobBuilder("DedupCustomerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(dedupCustomerStep())
                .end()
                .build();
    }

    @Bean
    public Step dedupCustomerStep() {
        return new StepBuilder("DedupCustomerStep", jobRepository)
                .<OldCustomer, Customer>chunk(10, transactionManager)
                .reader(oldCustomerReader)
                .processor(dedupProcessor)
                .writer(customerItemWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

}
