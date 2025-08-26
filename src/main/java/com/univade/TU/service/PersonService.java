package com.univade.TU.service;

import com.univade.TU.entity.Person;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person createPerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }
        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public Person getPersonById(String cin) {
        if (cin == null) {
            throw new IllegalArgumentException("CIN cannot be null");
        }
        return personRepository.findById(cin)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with CIN: " + cin));
    }

    @Transactional(readOnly = true)
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Person> getAllPersons(Pageable pageable) {
        return personRepository.findAll(pageable);
    }

    public Person updatePerson(String cin, Person person) {
        if (cin == null) {
            throw new IllegalArgumentException("CIN cannot be null");
        }
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }
        
        Person existingPerson = getPersonById(cin);
        existingPerson.setFirstName(person.getFirstName());
        existingPerson.setLastName(person.getLastName());
        existingPerson.setDateOfBirth(person.getDateOfBirth());
        existingPerson.setPhoneNumber(person.getPhoneNumber());
        existingPerson.setEmail(person.getEmail());
        
        return personRepository.save(existingPerson);
    }

    public void deletePerson(String cin) {
        if (cin == null) {
            throw new IllegalArgumentException("CIN cannot be null");
        }
        if (!personRepository.existsById(cin)) {
            throw new EntityNotFoundException("Person not found with CIN: " + cin);
        }
        personRepository.deleteById(cin);
    }

    @Transactional(readOnly = true)
    public boolean existsPersonById(String cin) {
        if (cin == null) {
            return false;
        }
        return personRepository.existsById(cin);
    }

    @Transactional(readOnly = true)
    public long countPersons() {
        return personRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<Person> findPersonByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Person> findPersonsByFirstName(String firstName) {
        return personRepository.findByFirstName(firstName);
    }

    @Transactional(readOnly = true)
    public List<Person> findPersonsByLastName(String lastName) {
        return personRepository.findByLastName(lastName);
    }
}
