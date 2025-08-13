# Spring Boot Batch Processing Demo

## Overview
This project demonstrates a comprehensive implementation of **Spring Batch 5.0** framework for processing large volumes of data efficiently. The application reads person data from a CSV file, processes it, and stores it in a database using batch processing patterns.

## Spring Batch Theory

### What is Spring Batch?
Spring Batch is a lightweight, comprehensive batch framework designed to enable the development of robust batch applications. It provides reusable functions essential in processing large volumes of records, including logging/tracing, transaction management, job processing statistics, job restart, skip, and resource management.

### Core Concepts

#### 1. Job
A Job is an entity that encapsulates an entire batch process. It serves as a container for Steps and defines how those steps are ordered and executed.

#### 2. Step 
A Step is a domain object that encapsulates an independent, sequential phase of a batch job. Every Job is composed of one or more steps.

#### 3. ItemReader
An ItemReader is responsible for retrieving data from various sources (files, databases, message queues, etc.) one item at a time.

#### 4. ItemProcessor
An ItemProcessor transforms or processes the data retrieved by ItemReader before it's written by ItemWriter.

#### 5. ItemWriter
An ItemWriter is responsible for writing data to various destinations (databases, files, message queues, etc.).

#### 6. Job Repository
JobRepository is responsible for persistence of batch meta-data entities (JobExecution, StepExecution, etc.).

#### 7. Job Launcher
JobLauncher is responsible for starting a Job with a given set of JobParameters.

## Spring Batch Processing Flow

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│             │    │              │    │             │    │              │
│ CSV File    │───▶│ ItemReader   │───▶│ItemProcessor│───▶│ ItemWriter   │
│ (Input)     │    │ (Read Data)  │    │(Transform)  │    │(Save to DB)  │
│             │    │              │    │             │    │              │
└─────────────┘    └──────────────┘    └─────────────┘    └──────────────┘
                            │                                      │
                            ▼                                      ▼
                   ┌──────────────┐                      ┌──────────────┐
                   │              │                      │              │
                   │   Step       │                      │  Database    │
                   │ (Chunk=10)   │                      │  (Person     │
                   │              │                      │   Entity)    │
                   └──────────────┘                      └──────────────┘
                            │
                            ▼
                   ┌──────────────┐
                   │              │
                   │     Job      │
                   │ (importPersons)│
                   │              │
                   └──────────────┘
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Spring Batch Architecture                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────────────────────┐ │
│  │             │    │              │    │                                 │ │
│  │ JobController│───▶│ JobLauncher  │───▶│         Job Repository          │ │
│  │             │    │              │    │      (Meta-data Storage)        │ │
│  └─────────────┘    └──────────────┘    └─────────────────────────────────┘ │
│                              │                                               │
│                              ▼                                               │
│                     ┌──────────────┐                                        │
│                     │              │                                        │
│                     │     Job      │                                        │
│                     │              │                                        │
│                     └──────────────┘                                        │
│                              │                                               │
│                              ▼                                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         Step                                        │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │             │  │             │  │             │                 │   │
│  │  │ItemReader   │─▶│ItemProcessor│─▶│ItemWriter   │                 │   │
│  │  │(CSV File)   │  │(Transform)  │  │(Database)   │                 │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Project Structure Analysis

### 1. Entity Layer (`Person.java`)
```java
@Entity
@Data
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ... other fields
}
```
**Analysis:** This is a JPA entity representing the person data structure that will be stored in the database. The `@Data` annotation from Lombok automatically generates getters, setters, and other utility methods.

### 2. Controller Layer (`JobController.java`)
```java
@RestController
public class JobController {
    @PostMapping("/importData")
    public String jobLauncher() {
        // Job execution logic
    }
}
```
**Analysis:** This REST controller provides an endpoint to trigger the batch job manually. It uses `JobLauncher` to execute the job with current timestamp as a parameter to ensure unique job instances.

### 3. Configuration Layer (`SpringBatchConfig.java`)

#### ItemReader Configuration
```java
@Bean
public FlatFileItemReader<Person> reader() {
    return new FlatFileItemReaderBuilder<Person>()
        .name("personItemReader")
        .resource(new ClassPathResource("people-1000.csv"))
        .linesToSkip(1)
        .lineMapper(lineMapper())
        .build();
}
```
**Analysis:** Configures a flat file reader to read CSV data. It skips the header line and uses a custom line mapper to convert CSV rows to Person objects.

#### ItemProcessor Configuration
```java
@Bean
PersonProcessor processor() {
    return new PersonProcessor();
}
```
**Analysis:** Registers the custom processor that transforms data before writing to the database.

#### ItemWriter Configuration
```java
@Bean
RepositoryItemWriter<Person> writer() {
    RepositoryItemWriter<Person> writer = new RepositoryItemWriter<>();
    writer.setRepository(repository);
    writer.setMethodName("save");
    return writer;
}
```
**Analysis:** Configures a repository-based writer that uses Spring Data JPA repository to save entities to the database.

### 4. Processor Layer (`PersonProcessor.java`)
```java
public class PersonProcessor implements ItemProcessor<Person,Person> {
    @Override
    public Person process(Person person) throws Exception {
        person.setFirstName(person.getFirstName().toUpperCase());
        person.setLastName(person.getLastName().toUpperCase());
        return person;
    }
}
```
**Analysis:** This processor transforms the first and last names to uppercase before saving to the database. This demonstrates data transformation capabilities in the batch processing pipeline.

## Key Features

- **Chunk-based Processing**: Processes data in chunks of 10 items for optimal memory usage
- **Transaction Management**: Each chunk is processed in a separate transaction
- **Error Handling**: Comprehensive exception handling for job execution
- **RESTful Job Triggering**: HTTP endpoint to start batch jobs
- **Data Transformation**: Uppercase conversion of names during processing
- **Spring Boot Integration**: Seamless integration with Spring Boot ecosystem

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Spring Boot 3.x
- Spring Batch 5.0
- Database (H2/MySQL/PostgreSQL)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd spring-batch-demo
   ```

2. **Prepare your CSV file**
   - Place your `people-1000.csv` file in `src/main/resources/`
   - Ensure CSV format matches: `userId,firstName,lastName,gender,email,phone,dateOfBirth,jobTitle`

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Trigger the batch job**
   ```bash
   curl -X POST http://localhost:8080/importData
   ```

## CSV File Format

Your CSV file should follow this format:
```csv
userId,firstName,lastName,gender,email,phone,dateOfBirth,jobTitle
1,John,Doe,Male,john.doe@email.com,123-456-7890,1990-01-01,Software Engineer
2,Jane,Smith,Female,jane.smith@email.com,098-765-4321,1985-05-15,Product Manager
```

## Job Execution Flow

1. **Job Trigger**: HTTP POST request to `/importData` endpoint
2. **Job Launch**: JobLauncher starts the job with unique parameters
3. **Step Execution**: Single step processes the CSV file
4. **Chunk Processing**: Data is processed in chunks of 10 items
5. **Read Phase**: FlatFileItemReader reads CSV records
6. **Process Phase**: PersonProcessor transforms names to uppercase  
7. **Write Phase**: RepositoryItemWriter saves to database
8. **Job Completion**: Returns execution status

## Database Schema

The application automatically creates a `Person` table with the following structure:
```sql
CREATE TABLE Person (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    gender VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    date_of_birth VARCHAR(255),
    job_title VARCHAR(255)
);
```

## Spring Batch Meta-Data Tables

Spring Batch automatically creates several meta-data tables:
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_JOB_EXECUTION_PARAMS`
- `BATCH_STEP_EXECUTION`
- `BATCH_JOB_EXECUTION_CONTEXT`
- `BATCH_STEP_EXECUTION_CONTEXT`

## Performance Considerations

- **Chunk Size**: Set to 10 items for balanced memory usage and transaction size
- **Transaction Boundaries**: Each chunk is processed in a separate transaction
- **Memory Management**: Stream-based reading prevents loading entire file into memory
- **Database Connections**: Connection pooling is handled by Spring Boot

## Error Handling

The application handles various batch job exceptions:
- `JobInstanceAlreadyCompleteException`
- `JobExecutionAlreadyRunningException`
- `JobParametersInvalidException`
- `JobRestartException`

## Monitoring and Logging

- Job execution status is returned via REST API
- Spring Batch meta-data tables store execution history
- Application logs provide detailed processing information

## Future Enhancements

- Add job scheduling with Spring Scheduler
- Implement job restart and skip functionality
- Add validation for input data
- Implement parallel processing for large datasets
- Add metrics and monitoring with Micrometer

## Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-batch</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## License

This project is licensed under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
