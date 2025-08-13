package com.demo.springbatch.config;

import com.demo.springbatch.entity.Person;
import com.demo.springbatch.repo.PersonRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

//@EnableBatchProcessing // not recommended now
@Configuration
public class SpringBatchConfig {

    @Autowired
    PersonRepository repository;

    @Bean
    public FlatFileItemReader<Person> reader(){
        return  new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("people-1000.csv")) // read this file from the folder
                .linesToSkip(1) // how many lines to skip in our csv file
                .lineMapper(lineMapper()) //  custom step to read the file.
                .targetType(Person.class)
                .build();
    }

    private LineMapper<Person> lineMapper(){
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");// required "," separated
        tokenizer.setStrict(false); // if true then number of tokens in line must match the number of tokens defined else with less token
// will be tolerated and appended with empty
        tokenizer.setNames("userId","firstName","lastName","gender","email","phone","dateOfBirth","jobTitle"); // header names


        BeanWrapperFieldSetMapper<Person> filedMapper = new BeanWrapperFieldSetMapper<>();
        filedMapper.setTargetType(Person.class); // this will map above line number 49 to our entity

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(filedMapper);

        return lineMapper;

    }

    @Bean
    PersonProcessor processor(){
        return  new PersonProcessor();
    }

    @Bean
    RepositoryItemWriter<Person> writer(){
        RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Job job(JobRepository jobRepository,Step step){
        return new JobBuilder("importPersons",jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return  new StepBuilder("csv-import-step",jobRepository)
                .<Person,Person>chunk(10,transactionManager) //process the items in chunks with the size provider
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }
}
