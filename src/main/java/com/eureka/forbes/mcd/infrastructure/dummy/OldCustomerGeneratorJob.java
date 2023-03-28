package com.eureka.forbes.mcd.infrastructure.dummy;

import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Takes 4m53s649ms to generate 5 million customers.
 * Customer are generated as per comment in OldCustomerGenerator.
 * Uses multiple threads.
 */
@Configuration
@Profile("generate_dummy")
public class OldCustomerGeneratorJob {

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobExecutionListener listener;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private OldCustomerGenerator oldCustomerGenerator;

    @Bean
    public ItemWriter<OldCustomer> oldCustomerWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<OldCustomer>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO old_customer (name, dob, phone_number, address, creation_time, modification_time)" +
                        " VALUES (:name, :dob, :phoneNumber, :address, :creationTime, :modificationTime)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job oldCustomerJob(Step oldCusomterStep) {
        return new JobBuilder("oldCustomerGenerator", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(oldCusomterStep)
                .end()
                .build();
    }

    @Bean
    public Step oldCusomterStep(ItemWriter<OldCustomer> oldCustomerWriter) {
        return new StepBuilder("oldCustomerStep", jobRepository)
                .<OldCustomer, OldCustomer>chunk(1000, transactionManager)
                .reader(oldCustomerGenerator)
                .writer(oldCustomerWriter)
                .taskExecutor(taskExecutor)
                .build();
    }
}
