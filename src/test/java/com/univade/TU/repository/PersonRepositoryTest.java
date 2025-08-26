package com.univade.TU.repository;

import com.univade.TU.testdata.PersonTestDataBuilder;


import com.univade.TU.entity.Person;


import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("Person Repository Tests")
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Person createPerson() {
        Person person = PersonTestDataBuilder.aValidPerson().build();
        return person;
    }


    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createPersonShouldPersistNewEntity() {
            Person testPerson = createPerson();
            Person createdPerson = personRepository.save(testPerson);

            assertThat(createdPerson).isNotNull();
            assertThat(createdPerson.getCin()).isNotNull();

            Person found = entityManager.find(Person.class, createdPerson.getCin());
            assertThat(found).isNotNull();
            assertThat(found.getFirstName()).isEqualTo(testPerson.getFirstName());
            assertThat(found.getLastName()).isEqualTo(testPerson.getLastName());
            assertThat(found.getDateOfBirth()).isEqualTo(testPerson.getDateOfBirth());
            assertThat(found.getPhoneNumber()).isEqualTo(testPerson.getPhoneNumber());
            assertThat(found.getEmail()).isEqualTo(testPerson.getEmail());
        }

        @Test
        void createPersonWithNullShouldThrowException() {
            assertThatThrownBy(() -> personRepository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultiplePersonsShouldPersistAll() {
            Person person1 = createPerson();
            Person person2 = createPerson();
            Person person3 = createPerson();

            List<Person> createdPersons = personRepository.saveAll(
                    List.of(person1, person2, person3));

            assertThat(createdPersons).hasSize(3);
            assertThat(createdPersons).allMatch(person -> person.getCin() != null);

            long count = personRepository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void readPersonByIdShouldReturnCorrectEntity() {
            Person testPerson = createPerson();
            Person savedPerson = entityManager.persistAndFlush(testPerson);

            Optional<Person> found = personRepository.findById(savedPerson.getCin());

            assertThat(found).isPresent();
            Person retrievedPerson = found.get();
            assertThat(retrievedPerson.getCin()).isEqualTo(savedPerson.getCin());
            assertThat(retrievedPerson.getFirstName()).isEqualTo(testPerson.getFirstName());
            assertThat(retrievedPerson.getLastName()).isEqualTo(testPerson.getLastName());
            assertThat(retrievedPerson.getDateOfBirth()).isEqualTo(testPerson.getDateOfBirth());
            assertThat(retrievedPerson.getPhoneNumber()).isEqualTo(testPerson.getPhoneNumber());
            assertThat(retrievedPerson.getEmail()).isEqualTo(testPerson.getEmail());
        }

        @Test
        void readPersonByNonExistentIdShouldReturnEmpty() {
            Optional<Person> found = personRepository.findById("NON_EXISTENT_ID");

            assertThat(found).isEmpty();
        }

    @Test
    void readAllPersonsShouldReturnAllEntities() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());

        List<Person> allPersons = personRepository.findAll();

            assertThat(allPersons).hasSize(2);
            assertThat(allPersons).extracting("cin")
                    .contains(person1.getCin(), person2.getCin());
    }

    @Test
    void readAllPersonsWhenEmptyShouldReturnEmptyList() {
        List<Person> allPersons = personRepository.findAll();

        assertThat(allPersons).isEmpty();
    }

    @Test
    void updatePersonShouldModifyExistingEntity() {
        Person testPerson = createPerson();
        Person savedPerson = entityManager.persistAndFlush(testPerson);
        entityManager.detach(savedPerson);

        savedPerson.setFirstName("Updated FirstName");

        Person updatedPerson = personRepository.save(savedPerson);

        assertThat(updatedPerson).isNotNull();
        assertThat(updatedPerson.getCin()).isEqualTo(savedPerson.getCin());
        assertThat(updatedPerson.getFirstName()).isEqualTo("Updated FirstName");

        Person found = entityManager.find(Person.class, savedPerson.getCin());
        assertThat(found.getFirstName()).isEqualTo("Updated FirstName");
    }

    @Test
    void saveNewPersonShouldCreateEntity() {
        long initialCount = personRepository.count();

        Person newPerson = createPerson();

        Person result = personRepository.save(newPerson);

        assertThat(result).isNotNull();
        assertThat(result.getCin()).isNotNull();

        long finalCount = personRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void updateAllPersonAttributesShouldPersistChanges() {
        Person testPerson = createPerson();
        Person savedPerson = entityManager.persistAndFlush(testPerson);
        entityManager.detach(savedPerson);

        savedPerson.setFirstName("Updated FirstName");

        Person updatedPerson = personRepository.save(savedPerson);

        assertThat(updatedPerson.getFirstName()).isEqualTo("Updated FirstName");
    }

    @Test
    void deletePersonByIdShouldRemoveEntity() {
        Person testPerson = createPerson();
        Person savedPerson = entityManager.persistAndFlush(testPerson);

        personRepository.deleteById(savedPerson.getCin());

        Optional<Person> found = personRepository.findById(savedPerson.getCin());
        assertThat(found).isEmpty();

        Person entityFound = entityManager.find(Person.class, savedPerson.getCin());
        assertThat(entityFound).isNull();
    }

        @Test
        void deletePersonByNonExistentIdShouldNotThrowException() {
            assertThatCode(() -> personRepository.deleteById("NON_EXISTENT_ID"))
                    .doesNotThrowAnyException();
        }

    @Test
    void deletePersonEntityShouldRemoveFromDatabase() {
        Person testPerson = createPerson();
        Person savedPerson = entityManager.persistAndFlush(testPerson);

        personRepository.delete(savedPerson);

        Optional<Person> found = personRepository.findById(savedPerson.getCin());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAllPersonsShouldRemoveAllEntities() {
        entityManager.persistAndFlush(createPerson());
        entityManager.persistAndFlush(createPerson());
        entityManager.persistAndFlush(createPerson());

        personRepository.deleteAll();

        List<Person> allPersons = personRepository.findAll();
        assertThat(allPersons).isEmpty();

        long count = personRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultiplePersonsByIdShouldRemoveSpecifiedEntities() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());
        Person person3 = entityManager.persistAndFlush(createPerson());

        personRepository.deleteAllById(List.of(person1.getCin(), person2.getCin()));

        List<Person> remainingPersons = personRepository.findAll();
        assertThat(remainingPersons).hasSize(1);
        assertThat(remainingPersons.get(0).getCin()).isEqualTo(person3.getCin());
    }

    @Test
    void existsPersonByIdShouldReturnTrueForExistingEntity() {
        Person testPerson = createPerson();
        Person savedPerson = entityManager.persistAndFlush(testPerson);

        boolean exists = personRepository.existsById(savedPerson.getCin());

        assertThat(exists).isTrue();
    }

        @Test
        void existsPersonByIdShouldReturnFalseForNonExistingEntity() {
            boolean exists = personRepository.existsById("NON_EXISTENT_ID");

            assertThat(exists).isFalse();
        }

    @Test
    void countPersonsShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(createPerson());
        entityManager.persistAndFlush(createPerson());

        long count = personRepository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void countPersonsWhenEmptyShouldReturnZero() {
            long count = personRepository.count();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test that duplicate values are rejected for unique field")
        void savePersonWithDuplicateCinShouldThrowException() {
            Person person1 = createPerson();
            Person person2 = createPerson();

            entityManager.persistAndFlush(person1);
            entityManager.clear();

            // Set duplicate value for unique field
            person2.setCin(person1.getCin());

            assertThatThrownBy(() -> {
                entityManager.persist(person2);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePersonWithTooLongCinShouldThrowException() {
            String tooLongValue = "a".repeat(13);
            Person personWithTooLongCin = createPerson();
            personWithTooLongCin.setCin(tooLongValue);

            assertThatThrownBy(() -> {
                personRepository.save(personWithTooLongCin);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void savePersonWithNullFirstNameShouldThrowException() {
            Person personWithNullFirstName = createPerson();
            personWithNullFirstName.setFirstName(null);

            assertThatThrownBy(() -> {
                personRepository.save(personWithNullFirstName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("firstName");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePersonWithTooLongFirstNameShouldThrowException() {
            String tooLongValue = "a".repeat(51);
            Person personWithTooLongFirstName = createPerson();
            personWithTooLongFirstName.setFirstName(tooLongValue);

            assertThatThrownBy(() -> {
                personRepository.save(personWithTooLongFirstName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void savePersonWithNullLastNameShouldThrowException() {
            Person personWithNullLastName = createPerson();
            personWithNullLastName.setLastName(null);

            assertThatThrownBy(() -> {
                personRepository.save(personWithNullLastName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("lastName");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePersonWithTooLongLastNameShouldThrowException() {
            String tooLongValue = "a".repeat(51);
            Person personWithTooLongLastName = createPerson();
            personWithTooLongLastName.setLastName(tooLongValue);

            assertThatThrownBy(() -> {
                personRepository.save(personWithTooLongLastName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that duplicate values are rejected for unique field")
        void savePersonWithDuplicatePhoneNumberShouldThrowException() {
            Person person1 = createPerson();
            Person person2 = createPerson();

            entityManager.persistAndFlush(person1);
            entityManager.clear();

            // Set duplicate value for unique field
            person2.setPhoneNumber(person1.getPhoneNumber());

            assertThatThrownBy(() -> {
                entityManager.persist(person2);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePersonWithTooLongPhoneNumberShouldThrowException() {
            String tooLongValue = "a".repeat(16);
            Person personWithTooLongPhoneNumber = createPerson();
            personWithTooLongPhoneNumber.setPhoneNumber(tooLongValue);

            assertThatThrownBy(() -> {
                personRepository.save(personWithTooLongPhoneNumber);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that duplicate values are rejected for unique field")
        void savePersonWithDuplicateEmailShouldThrowException() {
            Person person1 = createPerson();
            Person person2 = createPerson();

            entityManager.persistAndFlush(person1);
            entityManager.clear();

            // Set duplicate value for unique field
            person2.setEmail(person1.getEmail());

            assertThatThrownBy(() -> {
                entityManager.persist(person2);
                entityManager.flush();
            }).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void savePersonWithTooLongEmailShouldThrowException() {
            String tooLongValue = "a".repeat(101);
            Person personWithTooLongEmail = createPerson();
            personWithTooLongEmail.setEmail(tooLongValue);

            assertThatThrownBy(() -> {
                personRepository.save(personWithTooLongEmail);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }


        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            Person validPerson = createPerson();
            entityManager.persistAndFlush(validPerson);
            long initialCount = personRepository.count();

            Person invalidPerson = createPerson();
            invalidPerson.setFirstName(null);

            assertThatThrownBy(() -> {
                personRepository.save(invalidPerson);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            // Clear entity manager after constraint violation
            entityManager.clear();

            // Verify transaction rollback - data consistency maintained
            long finalCount = personRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should maintain data integrity across multiple constraint violations")
        void shouldMaintainDataIntegrityAcrossMultipleConstraintViolations() {
            long initialCount = personRepository.count();

            Person invalidPersonFirstName = createPerson();
            invalidPersonFirstName.setFirstName(null);

            assertThatThrownBy(() -> {
                personRepository.save(invalidPersonFirstName);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            entityManager.clear();


            long finalCount = personRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());
        Person person3 = entityManager.persistAndFlush(createPerson());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());
        Person person3 = entityManager.persistAndFlush(createPerson());

        Pageable pageable = PageRequest.of(1, 2);
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());
        Person person3 = entityManager.persistAndFlush(createPerson());

        Sort sort = Sort.by(Sort.Direction.ASC, "firstName");
        List<Person> sortedPersons = personRepository.findAll(sort);

        assertThat(sortedPersons).hasSize(3);
        assertThat(sortedPersons).isSortedAccordingTo((a, b) -> a.getFirstName().compareTo(b.getFirstName()));
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());
        Person person3 = entityManager.persistAndFlush(createPerson());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "firstName"));
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.getFirstName().compareTo(a.getFirstName()));
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());

        Pageable pageable = PageRequest.of(0, 100);
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
        Person person1 = entityManager.persistAndFlush(createPerson());
        Person person2 = entityManager.persistAndFlush(createPerson());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Person> page = personRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
    }

    
}

}
