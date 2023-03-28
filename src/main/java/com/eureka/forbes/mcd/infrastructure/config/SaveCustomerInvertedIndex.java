package com.eureka.forbes.mcd.infrastructure.config;

import com.eureka.forbes.mcd.infrastructure.enitity.es.EsCustomer;
import com.eureka.forbes.mcd.infrastructure.enitity.es.EsCustomerRepository;
import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Took 15m32s17ms for 5million records.
 * Simply uses batch saveAll to save batch of records to elastic search.
 * Uses multiple threads.
 */
@Configuration
@Profile("save_es")
public class SaveCustomerInvertedIndex {

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobExecutionListener listener;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private EsCustomerRepository esCustomerRepository;

    @Autowired
    private ItemReader<? extends OldCustomer> oldCustomerReader;

    @Bean
    public ItemProcessor<OldCustomer, EsCustomer> mapToEs() {
        return o -> EsCustomer.from(o);
    }

    @Bean
    public ItemWriter<EsCustomer> esCustomerWriter() {
        return chunk -> esCustomerRepository.saveAll(chunk);
    }

    @Bean
    public Job oldCustomerJob(Step esCusomterStep) {
        return new JobBuilder("SaveCustomerInvertedIndex", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(esCusomterStep)
                .end()
                .build();
    }

    @Bean
    public Step esCusomterStep() {
        return new StepBuilder("esCustomerStep", jobRepository)
                .<OldCustomer, EsCustomer>chunk(1000, transactionManager)
                .reader(oldCustomerReader)
                .processor(mapToEs())
                .writer(esCustomerWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

}
