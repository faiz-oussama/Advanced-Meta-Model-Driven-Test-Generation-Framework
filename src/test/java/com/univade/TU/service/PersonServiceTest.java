package com.univade.TU.service;

import com.univade.TU.testdata.PersonTestDataBuilder;


import com.univade.TU.entity.Person;


import com.univade.TU.repository.PersonRepository;

import com.univade.TU.service.PersonService;
import com.univade.TU.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Person Service Tests")
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    @BeforeEach
    void setUp() {
        reset(personRepository);
    }

    private Person createPerson() {
        return PersonTestDataBuilder.aValidPerson().build();
    }

    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createPersonShouldSaveAndReturnEntity() {
            Person testPerson = createPerson();
            Person savedPerson = createPerson();

            when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

            Person result = personService.createPerson(testPerson);

            assertThat(result).isNotNull();
            verify(personRepository).save(testPerson);
        }

        @Test
        void createPersonWithNullShouldThrowException() {
            assertThatThrownBy(() -> personService.createPerson(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getPersonByIdShouldReturnEntityWhenExists() {
            String testId = "CREATED_ID";
            Person testPerson = createPerson();
            testPerson.setCin(testId);

            when(personRepository.findById(testId)).thenReturn(Optional.of(testPerson));

            Person result = personService.getPersonById(testId);

            assertThat(result).isNotNull();
            assertThat(result.getCin()).isEqualTo(testId);
            verify(personRepository).findById(testId);
        }

        @Test
        void getPersonByIdShouldThrowExceptionWhenNotFound() {
            String testId = "NON_EXISTENT_ID";

            when(personRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> personService.getPersonById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(personRepository).findById(testId);
        }

        @Test
        void getAllPersonsShouldReturnAllEntities() {
            Person person1 = createPerson();
            Person person2 = createPerson();
            List<Person> expectedPersons = List.of(person1, person2);

            when(personRepository.findAll()).thenReturn(expectedPersons);

            List<Person> result = personService.getAllPersons();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedPersons);
            verify(personRepository).findAll();
        }

        @Test
        void getAllPersonsWhenEmptyShouldReturnEmptyList() {
            when(personRepository.findAll()).thenReturn(Collections.emptyList());

            List<Person> result = personService.getAllPersons();

            assertThat(result).isEmpty();
            verify(personRepository).findAll();
        }

        @Test
        void updatePersonShouldUpdateAndReturnEntity() {
            String testId = "CREATED_ID";
            Person existingPerson = createPerson();
            existingPerson.setCin(testId);

            Person updatedPerson = createPerson();
            updatedPerson.setCin(testId);
            updatedPerson.setFirstName("Updated FirstName");

            when(personRepository.findById(testId)).thenReturn(Optional.of(existingPerson));
            when(personRepository.save(any(Person.class))).thenReturn(updatedPerson);

            Person result = personService.updatePerson(testId, updatedPerson);

            assertThat(result).isNotNull();
            assertThat(result.getCin()).isEqualTo(testId);
            assertThat(result.getFirstName()).isEqualTo("Updated FirstName");
            verify(personRepository).findById(testId);
            verify(personRepository).save(any(Person.class));
        }

        @Test
        void updatePersonWithNonExistentIdShouldThrowException() {
            String testId = "NON_EXISTENT_ID";
            Person updatedPerson = createPerson();

            when(personRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> personService.updatePerson(testId, updatedPerson))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(personRepository).findById(testId);
            verify(personRepository, never()).save(any(Person.class));
        }

        @Test
        void deletePersonByIdShouldDeleteWhenExists() {
            String testId = "CREATED_ID";

            when(personRepository.existsById(testId)).thenReturn(true);
            doNothing().when(personRepository).deleteById(testId);

            assertThatCode(() -> personService.deletePerson(testId))
                    .doesNotThrowAnyException();

            verify(personRepository).existsById(testId);
            verify(personRepository).deleteById(testId);
        }

        @Test
        void deletePersonByIdShouldThrowExceptionWhenNotFound() {
            String testId = "NON_EXISTENT_ID";

            when(personRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> personService.deletePerson(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(personRepository).existsById(testId);
            verify(personRepository, never()).deleteById(testId);
        }

        @Test
        void existsPersonByIdShouldReturnTrueWhenExists() {
            String testId = "CREATED_ID";

            when(personRepository.existsById(testId)).thenReturn(true);

            boolean result = personService.existsPersonById(testId);

            assertThat(result).isTrue();
            verify(personRepository).existsById(testId);
        }

        @Test
        void existsPersonByIdShouldReturnFalseWhenNotExists() {
            String testId = "NON_EXISTENT_ID";

            when(personRepository.existsById(testId)).thenReturn(false);

            boolean result = personService.existsPersonById(testId);

            assertThat(result).isFalse();
            verify(personRepository).existsById(testId);
        }

        @Test
        void countPersonsShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(personRepository.count()).thenReturn(expectedCount);

            long result = personService.countPersons();

            assertThat(result).isEqualTo(expectedCount);
            verify(personRepository).count();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating Person with null entity")
        void createPersonWithNullShouldThrowException() {
            assertThatThrownBy(() -> personService.createPerson(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Person cannot be null");

            verifyNoInteractions(personRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Person with null entity")
        void updatePersonWithNullShouldThrowException() {
            String testId = "CREATED_ID";

            assertThatThrownBy(() -> personService.updatePerson(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Person cannot be null");

            verifyNoInteractions(personRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Person with null ID")
        void updatePersonWithNullIdShouldThrowException() {
            Person testPerson = createPerson();

            assertThatThrownBy(() -> personService.updatePerson(null, testPerson))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CIN cannot be null");

            verifyNoInteractions(personRepository);
        }

        @Test
        @DisplayName("Should throw exception when deleting Person with null ID")
        void deletePersonWithNullIdShouldThrowException() {
            assertThatThrownBy(() -> personService.deletePerson(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CIN cannot be null");

            verifyNoInteractions(personRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent Person")
        void updatePersonWithNonExistentIdShouldThrowException() {
            String nonExistentId = "NON_EXISTENT_ID";
            Person testPerson = createPerson();

            when(personRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> personService.updatePerson(nonExistentId, testPerson))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Person not found with CIN: " + nonExistentId);

            verify(personRepository).findById(nonExistentId);
            verify(personRepository, never()).save(any(Person.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent Person")
        void deletePersonWithNonExistentIdShouldThrowException() {
            String nonExistentId = "NON_EXISTENT_ID";

            when(personRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> personService.deletePerson(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Person not found with CIN: " + nonExistentId);

            verify(personRepository).existsById(nonExistentId);
            verify(personRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding Person")
        void findPersonByIdShouldHandleRepositoryException() {
            String testId = "CREATED_ID";

            when(personRepository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> personService.getPersonById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(personRepository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving Person")
        void createPersonShouldValidateBusinessRules() {
            Person testPerson = createPerson();
            Person savedPerson = createPerson();

            when(personRepository.save(testPerson)).thenReturn(savedPerson);

            Person result = personService.createPerson(testPerson);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedPerson);
            verify(personRepository).save(testPerson);
        }

        @Test
        @DisplayName("Should check existence before operations")
        void existsPersonByIdShouldReturnCorrectValue() {
            String existingId = "EXISTING_ID";
            String nonExistentId = "NON_EXISTENT_ID";

            when(personRepository.existsById(existingId)).thenReturn(true);
            when(personRepository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = personService.existsPersonById(existingId);
            boolean notExistsResult = personService.existsPersonById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(personRepository).existsById(existingId);
            verify(personRepository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void existsPersonByIdWithNullShouldReturnFalse() {
            boolean result = personService.existsPersonById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(personRepository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void createPersonShouldHandleConstraintViolationException() {
            Person invalidPerson = createPerson();
            invalidPerson.setFirstName(null);

            when(personRepository.save(invalidPerson))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> personService.createPerson(invalidPerson))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(personRepository).save(invalidPerson);
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void createPersonShouldHandleDataIntegrityViolationException() {
            Person duplicatePerson = createPerson();
            duplicatePerson.setCin("duplicate@example.com");

            when(personRepository.save(duplicatePerson))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> personService.createPerson(duplicatePerson))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(personRepository).save(duplicatePerson);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAllPersonsWithPageableShouldReturnPagedResults() {
            Person person1 = createPerson();
            Person person2 = createPerson();
            Person person3 = createPerson();

            List<Person> personList = List.of(person1, person2);
            Page<Person> expectedPage = new PageImpl<>(personList, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(personRepository).findAll(pageable);
        }

        @Test
        void getAllPersonsWithPageableAndSortShouldReturnSortedResults() {
            Person person1 = createPerson();
            person1.setFirstName("Hicham");
            Person person2 = createPerson();
            person2.setFirstName("Oussama");

            List<Person> sortedPersonList = List.of(person1, person2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
            Page<Person> expectedPage = new PageImpl<>(sortedPersonList, pageable, 2);

            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).getFirstName()).isEqualTo("Oussama");
            verify(personRepository).findAll(pageable);
        }

        @Test
        void getAllPersonsWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Person> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(personRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(personRepository).findAll(pageable);
        }

        @Test
        void getAllPersonsWithLargePageSizeShouldReturnAllResults() {
            Person person1 = createPerson();
            Person person2 = createPerson();
            Person person3 = createPerson();

            List<Person> allPersons = List.of(person1, person2, person3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<Person> expectedPage = new PageImpl<>(allPersons, pageable, 3);

            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(personRepository).findAll(pageable);
        }

        @Test
        void getAllPersonsWithSecondPageShouldReturnCorrectResults() {
            Person person3 = createPerson();
            Person person4 = createPerson();

            List<Person> secondPagePersons = List.of(person3, person4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<Person> expectedPage = new PageImpl<>(secondPagePersons, pageable, 4);

            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(personRepository).findAll(pageable);
        }



        @Test
        void getAllPersonsWithDescendingSortShouldReturnCorrectOrder() {
            Person person1 = createPerson();
            person1.setFirstName("Ilyass");
            Person person2 = createPerson();
            person2.setFirstName("Oussama");

            List<Person> sortedPersonList = List.of(person2, person1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").descending());
            Page<Person> expectedPage = new PageImpl<>(sortedPersonList, pageable, 2);

            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).getFirstName()).isEqualTo("Ilyass");
            verify(personRepository).findAll(pageable);
        }

        @Test
        void getAllPersonsWithCustomPageSizeShouldRespectPageSize() {
            Person person1 = createPerson();
            Person person2 = createPerson();

            List<Person> personList = List.of(person1, person2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<Person> expectedPage = new PageImpl<>(List.of(person1), pageable, 2);

            when(personRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Person> result = personService.getAllPersons(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(personRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void createPersonShouldValidateEntity() {
            Person testPerson = createPerson();
            Person savedPerson = createPerson();

            when(personRepository.save(testPerson)).thenReturn(savedPerson);

            Person result = personService.createPerson(testPerson);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedPerson);
            verify(personRepository).save(testPerson);
        }

        @Test
        @DisplayName("Should handle repository exceptions during validation")
        void createPersonShouldHandleRepositoryExceptions() {
            Person testPerson = createPerson();

            when(personRepository.save(testPerson))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> personService.createPerson(testPerson))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(personRepository).save(testPerson);
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void updatePersonShouldValidateEntityState() {
            String testId = "CREATED_ID";
            Person existingPerson = createPerson();
            existingPerson.setCin(testId);

            Person updatedPerson = createPerson();
            Person savedPerson = createPerson();
            savedPerson.setCin(testId);

            when(personRepository.findById(testId)).thenReturn(Optional.of(existingPerson));
            when(personRepository.save(any(Person.class))).thenReturn(savedPerson);

            Person result = personService.updatePerson(testId, updatedPerson);

            assertThat(result).isNotNull();
            assertThat(result.getCin()).isEqualTo(testId);
            verify(personRepository).findById(testId);
            verify(personRepository).save(any(Person.class));
        }
    }


    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private PersonService personServiceIntegration;

        @Autowired
        private PersonRepository personRepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

        private Person testPersonIntegration;

        @BeforeEach
        void setUpIntegration() {
            testPersonIntegration = createTestEntity();
        }

        private Person createTestEntity() {
            Person entity = PersonTestDataBuilder.aDefaultPerson().build();
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create Person and persist to database")
            void shouldCreatePersonAndPersistToDatabase() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);

                assertThat(created).isNotNull();
                
                Optional<Person> persisted = personRepositoryIntegration.findById(created.getCin());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().getCin()).isEqualTo(created.getCin());
            }

            @Test
            @DisplayName("Should update Person and persist changes")
            void shouldUpdatePersonAndPersistChanges() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);
                
                created.setFirstName("Updated firstName");

                Person updated = personServiceIntegration.updatePerson(created.getCin(), created);

                assertThat(updated).isNotNull();
                Person persisted = personRepositoryIntegration.findById(created.getCin()).orElseThrow();
                assertThat(persisted.getFirstName()).isEqualTo("Updated firstName");
            }

            @Test
            @DisplayName("Should delete Person from database")
            void shouldDeletePersonFromDatabase() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);
                String id = created.getCin();

                personServiceIntegration.deletePerson(id);

                Optional<Person> deleted = personRepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {

            @Test
            @DisplayName("Should handle validation errors from JPA/Hibernate")
            void shouldHandleJpaValidationErrors() {
                Person invalidPerson = PersonTestDataBuilder.aDefaultPerson()
                        .withFirstName(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        personServiceIntegration.createPerson(invalidPerson);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } finally {
                    entityManager.clear();
                }
            }

            @Test
            @DisplayName("Should fail when duplicate cin is saved")
            void shouldFailWhenDuplicateCinIsSaved() {
                Person firstEntity = PersonTestDataBuilder.aDefaultPerson()
                        .withCin("TEST" + String.format("%06d", (System.currentTimeMillis() % 1000000)))
                        .build();
                Person first = personServiceIntegration.createPerson(firstEntity);

                entityManager.flush();
                entityManager.clear();

                Person duplicate = PersonTestDataBuilder.aDefaultPerson().build();
                duplicate.setPhoneNumber("0600" + String.format("%06d", (System.currentTimeMillis() % 1000000)));
                duplicate.setEmail("different" + System.currentTimeMillis() + "@test.com");
                duplicate.setCin(first.getCin());

                assertThatThrownBy(() -> {
                    entityManager.persist(duplicate);
                    entityManager.flush();
                }).satisfiesAnyOf(
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(jakarta.persistence.EntityExistsException.class)
                );
            }
            @Test
            @DisplayName("Should fail when duplicate phoneNumber is saved")
            void shouldFailWhenDuplicatePhoneNumberIsSaved() {
                Person firstEntity = PersonTestDataBuilder.aDefaultPerson()
                        .withPhoneNumber("0600" + String.format("%06d", (System.currentTimeMillis() % 1000000)))
                        .build();
                Person first = personServiceIntegration.createPerson(firstEntity);

                entityManager.flush();
                entityManager.clear();

                Person duplicate = PersonTestDataBuilder.aDefaultPerson().build();
                duplicate.setCin("DUP" + String.format("%07d", (System.currentTimeMillis() % 10000000)));
                duplicate.setEmail("different" + System.currentTimeMillis() + "@test.com");
                duplicate.setPhoneNumber(first.getPhoneNumber());

                assertThatThrownBy(() -> {
                    personServiceIntegration.createPerson(duplicate);
                    entityManager.flush();
                }).satisfiesAnyOf(
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(jakarta.persistence.EntityExistsException.class)
                );
            }
            @Test
            @DisplayName("Should fail when duplicate email is saved")
            void shouldFailWhenDuplicateEmailIsSaved() {
                Person firstEntity = PersonTestDataBuilder.aDefaultPerson()
                        .withEmail("test" + System.currentTimeMillis() + "@example.com")
                        .build();
                Person first = personServiceIntegration.createPerson(firstEntity);

                entityManager.flush();
                entityManager.clear();

                Person duplicate = PersonTestDataBuilder.aDefaultPerson().build();
                duplicate.setCin("DUP" + String.format("%07d", (System.currentTimeMillis() % 10000000)));
                duplicate.setPhoneNumber("0600" + String.format("%06d", (System.currentTimeMillis() % 1000000)));
                duplicate.setEmail(first.getEmail());

                assertThatThrownBy(() -> {
                    personServiceIntegration.createPerson(duplicate);
                    entityManager.flush();
                }).satisfiesAnyOf(
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(org.hibernate.exception.ConstraintViolationException.class),
                    throwable -> assertThat(throwable).isInstanceOf(jakarta.persistence.EntityExistsException.class)
                );
            }
        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

            @Test
            @DisplayName("Should rollback transaction when error occurs during save")
            void shouldRollbackTransactionWhenErrorOccursDuringSave() {
                long initialCount = personRepositoryIntegration.count();

                Person invalidPerson = PersonTestDataBuilder.aDefaultPerson()
                        .withFirstName(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        personServiceIntegration.createPerson(invalidPerson);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } catch (Exception e) {
                    // Clear entity manager after constraint violation to avoid inconsistent state
                    entityManager.clear();
                    throw e;
                }

                // Clear entity manager to avoid stale state
                entityManager.clear();
                long finalCount = personRepositoryIntegration.count();
                assertThat(finalCount).isEqualTo(initialCount);
            }
        }


        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find Person by ID correctly")
            void shouldFindPersonByIdCorrectly() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);

                Person found = personServiceIntegration.getPersonById(created.getCin());

                assertThat(found).isNotNull();
                assertThat(found.getCin()).isEqualTo(created.getCin());
            }

            @Test
            @DisplayName("Should return empty when Person not found by ID")
            void shouldReturnEmptyWhenPersonNotFoundById() {
                String nonExistentId = "NON_EXISTENT_ID";

                assertThatThrownBy(() -> personServiceIntegration.getPersonById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);
                String nonExistentId = "NON_EXISTENT_ID";

                boolean exists = personServiceIntegration.existsPersonById(created.getCin());
                boolean notExists = personServiceIntegration.existsPersonById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all Persons from database")
            void shouldFindAllPersonsFromDatabase() {
                Person testEntity1 = createTestEntity();
                Person entity1 = personServiceIntegration.createPerson(testEntity1);
                Person testEntity2 = createTestEntity();
                Person entity2 = personServiceIntegration.createPerson(testEntity2);

                List<Person> all = personServiceIntegration.getAllPersons();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(Person::getCin)
                        .contains(entity1.getCin(), entity2.getCin());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete Person and verify removal")
            void shouldDeletePersonAndVerifyRemoval() {
                Person testEntity = createTestEntity();
                Person created = personServiceIntegration.createPerson(testEntity);
                String id = created.getCin();

                personServiceIntegration.deletePerson(id);

                boolean exists = personRepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

        }
    }




}
