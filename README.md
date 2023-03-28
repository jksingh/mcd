# MCD: Master Customer Data.

POC to migrate legacy customer data to new data store.

Legacy data has duplicate customer records and needs to be de-duplicated based on Phone-number and address.

Approach is to insert all data into Elasticsearch and then query (using match_like_this) for each customer
to get different entries for the same customers.

After deduplication save the customer to the new data store keeping the original legacy customer ids for 
exhaustive information of customer across all entries.

Uses Spring batch Jobs each to 
1. generate dummy data of 5 million records
2. add all records to Elasticsearch
3. deduplicate the customer records and push to new data store.

Using Spring batch give ability to resume Job in case of failures. 

## Stats
1. Takes 4m53s649ms to generate 5 million customers
2. Takes 15m32s17ms for 5million records.
3. Process 10K records per minute. At this speed it will take 8 hours to complete all 5 million customers.

### Configuration
MacBook Pro with 2.3 GHz Quad-Core Intel Core i7 Processor, 16 GB 1600 MHz DDR3 RAM and 500 GB Flash Storage
Postgres and Elasticsearch running in docker.


## Setup
Update the Postgress and Elasticsearch endpoints in application.properties. Run following command to build.
```bash
./mvnw clean install
```

## Running
Following are the commands to run for each Job after the previous one is finished.

```bash
# Generate Dummy data 
java -jar -Dspring.profiles.active=generate_dummy target/mcd-0.0.1-SNAPSHOT.jar

# add all records to elasticsearch
java -jar -Dspring.profiles.active=save_es target/mcd-0.0.1-SNAPSHOT.jar

# Generate Dummy data 
java -jar -Dspring.profiles.active=dedup target/mcd-0.0.1-SNAPSHOT.jar

```

## Future work
Search query ES can be improved better and faster result. Right now it is using match_like_this.
Use Spring batch Partitioning to distribute deduplication tasks on other worker nodes.


