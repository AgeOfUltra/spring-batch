package com.demo.springbatch.repo;

import com.demo.springbatch.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person,Long> {
}
