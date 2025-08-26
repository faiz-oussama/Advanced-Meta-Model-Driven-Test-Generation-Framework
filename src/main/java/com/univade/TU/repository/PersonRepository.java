package com.univade.TU.repository;

import com.univade.TU.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {

    Optional<Person> findByEmail(String email);
    Optional<Person> findByPhoneNumber(String phoneNumber);
    List<Person> findByFirstName(String firstName);
    List<Person> findByLastName(String lastName);
    List<Person> findByFirstNameAndLastName(String firstName, String lastName);
    List<Person> findByDateOfBirth(LocalDate dateOfBirth);
    List<Person> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);
}
