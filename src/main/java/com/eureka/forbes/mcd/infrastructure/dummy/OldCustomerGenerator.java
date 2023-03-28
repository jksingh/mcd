package com.eureka.forbes.mcd.infrastructure.dummy;

import com.eureka.forbes.mcd.infrastructure.enitity.legacy.OldCustomer;
import com.github.javafaker.Faker;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This generates 5 million fake customers with 20% duplicates on phone number or (optionally shuffled) address.
 */
@Component
public class OldCustomerGenerator implements ItemReader<OldCustomer> {

    public static final int recordLimit = 5_000_000;
    public static final int duplicatesPercentage = 20;

    private final Random random;
    private final Faker faker;
    private final int duplicateModulo;
    private final int duplicatesCount;
    private final List<OldCustomer> tobeDuplicated;
    private int currentCount;

    public OldCustomerGenerator() {
        random = new Random(0);
        faker = new Faker(new Locale.Builder().setLanguage("en").setRegion("IN").build(), random);
        currentCount = 1;
        duplicateModulo = 100 / duplicatesPercentage;
        duplicatesCount = recordLimit * duplicatesPercentage / duplicateModulo / 100;
        tobeDuplicated = new ArrayList<>(duplicatesCount);
    }

    @Override
    public OldCustomer read() throws Exception {
        if (currentCount > recordLimit) return null;
        OldCustomer customer = null;
        if (currentCount % duplicateModulo == 0) {
            customer = loadFromDuplicates();
        } else {
            customer = OldCustomer.builder()
                    .name(faker.name().name())
                    .dob(faker.date().birthday(20, 80))
                    .phoneNumber(faker.phoneNumber().phoneNumber())
                    .address(faker.address().fullAddress())
                    .creationTime(new Timestamp(faker.date().past(20 * 365, TimeUnit.DAYS).getTime()))
                    .modificationTime(new Timestamp(faker.date().past(5 * 365, TimeUnit.DAYS).getTime()))
                    .build();
            if (currentCount < duplicatesCount) {
                tobeDuplicated.add(customer);
            }
        }

        currentCount++;
        return customer;
    }

    private OldCustomer loadFromDuplicates() {
        OldCustomer customer;
        OldCustomer d = tobeDuplicated.get(random.nextInt(tobeDuplicated.size()));
        String phoneNumber = d.getPhoneNumber();
        String address = d.getAddress();
        switch (random.nextInt(4)) {
            case 0:
                phoneNumber = faker.phoneNumber().phoneNumber();
                break;
            case 1:
                address = faker.address().fullAddress();
                break;
            case 2:
            case 3:
                phoneNumber = faker.phoneNumber().phoneNumber();
                List<String> addressShuffled = Arrays.asList(address.split(" "));
                Collections.shuffle(addressShuffled);
                address = addressShuffled.stream().collect(Collectors.joining(" "));
            default:
        }
        customer = OldCustomer.builder()
                .name(d.getName())
                .dob(d.getDob())
                .phoneNumber(phoneNumber)
                .address(address)
                .creationTime(new Timestamp(faker.date().past(20 * 365, TimeUnit.DAYS).getTime()))
                .modificationTime(new Timestamp(faker.date().past(5 * 365, TimeUnit.DAYS).getTime()))
                .build();
        return customer;
    }
}
