package com.demo.springbatch.config;

import com.demo.springbatch.entity.Person;
import org.springframework.batch.item.ItemProcessor;


//to process the data before inserting into the DB
public class PersonProcessor  implements ItemProcessor<Person,Person> {
    @Override
    public Person process(Person person) throws Exception {
        person.setFirstName(person.getFirstName().toUpperCase());
        person.setLastName(person.getLastName().toUpperCase());
        return person;
    }
}
