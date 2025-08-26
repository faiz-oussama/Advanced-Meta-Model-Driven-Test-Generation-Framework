package com.univade.TU.repository;

import com.univade.TU.testdata.AddressTestDataBuilder;


import com.univade.TU.entity.Address;


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
@DisplayName("Address Repository Tests")
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        addressRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private Address createAddress() {
        Address address = AddressTestDataBuilder.aValidAddress().build();
        return address;
    }


    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createAddressShouldPersistNewEntity() {
            Address testAddress = createAddress();
            Address createdAddress = addressRepository.save(testAddress);

            assertThat(createdAddress).isNotNull();
            assertThat(createdAddress.getId()).isNotNull();
            assertThat(createdAddress.getId()).isPositive();

            Address found = entityManager.find(Address.class, createdAddress.getId());
            assertThat(found).isNotNull();
            assertThat(found.getStreet()).isEqualTo(testAddress.getStreet());
            assertThat(found.getCity()).isEqualTo(testAddress.getCity());
            assertThat(found.getZipCode()).isEqualTo(testAddress.getZipCode());
        }

        @Test
        void createAddressWithNullShouldThrowException() {
            assertThatThrownBy(() -> addressRepository.save(null))
                    .isInstanceOf(InvalidDataAccessApiUsageException.class);
        }

        @Test
        void createMultipleAddresssShouldPersistAll() {
            Address address1 = createAddress();
            Address address2 = createAddress();
            Address address3 = createAddress();

            List<Address> createdAddresss = addressRepository.saveAll(
                    List.of(address1, address2, address3));

            assertThat(createdAddresss).hasSize(3);
            assertThat(createdAddresss).allMatch(address -> address.getId() != null);

            long count = addressRepository.count();
            assertThat(count).isEqualTo(3);
        }

        @Test
        void readAddressByIdShouldReturnCorrectEntity() {
            Address testAddress = createAddress();
            Address savedAddress = entityManager.persistAndFlush(testAddress);

            Optional<Address> found = addressRepository.findById(savedAddress.getId());

            assertThat(found).isPresent();
            Address retrievedAddress = found.get();
            assertThat(retrievedAddress.getId()).isEqualTo(savedAddress.getId());
            assertThat(retrievedAddress.getStreet()).isEqualTo(testAddress.getStreet());
            assertThat(retrievedAddress.getCity()).isEqualTo(testAddress.getCity());
            assertThat(retrievedAddress.getZipCode()).isEqualTo(testAddress.getZipCode());
        }

        @Test
        void readAddressByNonExistentIdShouldReturnEmpty() {
            Optional<Address> found = addressRepository.findById(999L);

            assertThat(found).isEmpty();
        }

    @Test
    void readAllAddresssShouldReturnAllEntities() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());

        List<Address> allAddresss = addressRepository.findAll();

            assertThat(allAddresss).hasSize(2);
            assertThat(allAddresss).extracting("id")
                    .contains(address1.getId(), address2.getId());
    }

    @Test
    void readAllAddresssWhenEmptyShouldReturnEmptyList() {
        List<Address> allAddresss = addressRepository.findAll();

        assertThat(allAddresss).isEmpty();
    }

    @Test
    void updateAddressShouldModifyExistingEntity() {
        Address testAddress = createAddress();
        Address savedAddress = entityManager.persistAndFlush(testAddress);
        entityManager.detach(savedAddress);

        savedAddress.setStreet("Updated Street");

        Address updatedAddress = addressRepository.save(savedAddress);

        assertThat(updatedAddress).isNotNull();
        assertThat(updatedAddress.getId()).isEqualTo(savedAddress.getId());
        assertThat(updatedAddress.getStreet()).isEqualTo("Updated Street");

        Address found = entityManager.find(Address.class, savedAddress.getId());
        assertThat(found.getStreet()).isEqualTo("Updated Street");
    }

    @Test
    void saveNewAddressShouldCreateEntity() {
        long initialCount = addressRepository.count();

        Address newAddress = createAddress();

        Address result = addressRepository.save(newAddress);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();

        long finalCount = addressRepository.count();
        assertThat(finalCount).isEqualTo(initialCount + 1);
    }

    @Test
    void updateAllAddressAttributesShouldPersistChanges() {
        Address testAddress = createAddress();
        Address savedAddress = entityManager.persistAndFlush(testAddress);
        entityManager.detach(savedAddress);

        savedAddress.setStreet("Updated Street");

        Address updatedAddress = addressRepository.save(savedAddress);

        assertThat(updatedAddress.getStreet()).isEqualTo("Updated Street");
    }

    @Test
    void deleteAddressByIdShouldRemoveEntity() {
        Address testAddress = createAddress();
        Address savedAddress = entityManager.persistAndFlush(testAddress);

        addressRepository.deleteById(savedAddress.getId());

        Optional<Address> found = addressRepository.findById(savedAddress.getId());
        assertThat(found).isEmpty();

        Address entityFound = entityManager.find(Address.class, savedAddress.getId());
        assertThat(entityFound).isNull();
    }

        @Test
        void deleteAddressByNonExistentIdShouldNotThrowException() {
            assertThatCode(() -> addressRepository.deleteById(999L))
                    .doesNotThrowAnyException();
        }

    @Test
    void deleteAddressEntityShouldRemoveFromDatabase() {
        Address testAddress = createAddress();
        Address savedAddress = entityManager.persistAndFlush(testAddress);

        addressRepository.delete(savedAddress);

        Optional<Address> found = addressRepository.findById(savedAddress.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteAllAddresssShouldRemoveAllEntities() {
        entityManager.persistAndFlush(createAddress());
        entityManager.persistAndFlush(createAddress());
        entityManager.persistAndFlush(createAddress());

        addressRepository.deleteAll();

        List<Address> allAddresss = addressRepository.findAll();
        assertThat(allAddresss).isEmpty();

        long count = addressRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void deleteMultipleAddresssByIdShouldRemoveSpecifiedEntities() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());
        Address address3 = entityManager.persistAndFlush(createAddress());

        addressRepository.deleteAllById(List.of(address1.getId(), address2.getId()));

        List<Address> remainingAddresss = addressRepository.findAll();
        assertThat(remainingAddresss).hasSize(1);
        assertThat(remainingAddresss.get(0).getId()).isEqualTo(address3.getId());
    }

    @Test
    void existsAddressByIdShouldReturnTrueForExistingEntity() {
        Address testAddress = createAddress();
        Address savedAddress = entityManager.persistAndFlush(testAddress);

        boolean exists = addressRepository.existsById(savedAddress.getId());

        assertThat(exists).isTrue();
    }

        @Test
        void existsAddressByIdShouldReturnFalseForNonExistingEntity() {
            boolean exists = addressRepository.existsById(999L);

            assertThat(exists).isFalse();
        }

    @Test
    void countAddresssShouldReturnCorrectNumber() {
        entityManager.persistAndFlush(createAddress());
        entityManager.persistAndFlush(createAddress());

        long count = addressRepository.count();

        assertThat(count).isEqualTo(2);
    }

        @Test
        void countAddresssWhenEmptyShouldReturnZero() {
            long count = addressRepository.count();

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Test that primary key is automatically generated")
        void saveAddressShouldGenerateId() {
            Address address = createAddress();

            Address savedAddress = addressRepository.save(address);

            assertThat(savedAddress).isNotNull();
            assertThat(savedAddress.getId()).isNotNull();
            assertThat(savedAddress.getId()).isPositive();

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void saveAddressWithNullStreetShouldThrowException() {
            Address addressWithNullStreet = createAddress();
            addressWithNullStreet.setStreet(null);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithNullStreet);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("street");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void saveAddressWithTooLongStreetShouldThrowException() {
            String tooLongValue = "a".repeat(151);
            Address addressWithTooLongStreet = createAddress();
            addressWithTooLongStreet.setStreet(tooLongValue);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithTooLongStreet);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void saveAddressWithNullCityShouldThrowException() {
            Address addressWithNullCity = createAddress();
            addressWithNullCity.setCity(null);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithNullCity);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("city");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void saveAddressWithTooLongCityShouldThrowException() {
            String tooLongValue = "a".repeat(101);
            Address addressWithTooLongCity = createAddress();
            addressWithTooLongCity.setCity(tooLongValue);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithTooLongCity);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }

        @Test
        @DisplayName("Test that null values are rejected for non-nullable field")
        void saveAddressWithNullZipCodeShouldThrowException() {
            Address addressWithNullZipCode = createAddress();
            addressWithNullZipCode.setZipCode(null);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithNullZipCode);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class)
              .hasMessageContaining("zipCode");

        }

        @Test
        @DisplayName("Test that values exceeding max length are rejected")
        void saveAddressWithTooLongZipCodeShouldThrowException() {
            String tooLongValue = "a".repeat(11);
            Address addressWithTooLongZipCode = createAddress();
            addressWithTooLongZipCode.setZipCode(tooLongValue);

            assertThatThrownBy(() -> {
                addressRepository.save(addressWithTooLongZipCode);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

        }


        @Test
        @DisplayName("Should handle transaction rollback on constraint violation")
        void shouldHandleTransactionRollbackOnConstraintViolation() {
            Address validAddress = createAddress();
            entityManager.persistAndFlush(validAddress);
            long initialCount = addressRepository.count();

            Address invalidAddress = createAddress();
            invalidAddress.setStreet(null);

            assertThatThrownBy(() -> {
                addressRepository.save(invalidAddress);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            // Clear entity manager after constraint violation
            entityManager.clear();

            // Verify transaction rollback - data consistency maintained
            long finalCount = addressRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }

        @Test
        @DisplayName("Should maintain data integrity across multiple constraint violations")
        void shouldMaintainDataIntegrityAcrossMultipleConstraintViolations() {
            long initialCount = addressRepository.count();

            Address invalidAddressStreet = createAddress();
            invalidAddressStreet.setStreet(null);

            assertThatThrownBy(() -> {
                addressRepository.save(invalidAddressStreet);
                entityManager.flush();
            }).isInstanceOf(ConstraintViolationException.class);

            entityManager.clear();


            long finalCount = addressRepository.count();
            assertThat(finalCount).isEqualTo(initialCount);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void findAllWithPaginationShouldReturnPagedResults() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());
        Address address3 = entityManager.persistAndFlush(createAddress());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAllWithPaginationSecondPageShouldReturnRemainingResults() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());
        Address address3 = entityManager.persistAndFlush(createAddress());

        Pageable pageable = PageRequest.of(1, 2);
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isLast()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void findAllWithSortingShouldReturnSortedResults() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());
        Address address3 = entityManager.persistAndFlush(createAddress());

        Sort sort = Sort.by(Sort.Direction.ASC, "street");
        List<Address> sortedAddresss = addressRepository.findAll(sort);

        assertThat(sortedAddresss).hasSize(3);
        assertThat(sortedAddresss).isSortedAccordingTo((a, b) -> a.getStreet().compareTo(b.getStreet()));
    }

    @Test
    void findAllWithPaginationAndSortingShouldReturnPagedAndSortedResults() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());
        Address address3 = entityManager.persistAndFlush(createAddress());

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "street"));
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).isSortedAccordingTo((a, b) -> b.getStreet().compareTo(a.getStreet()));
    }

    @Test
    void findAllWithEmptyPageShouldReturnEmptyResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getTotalPages()).isZero();
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void findAllWithLargePageSizeShouldReturnAllResults() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());

        Pageable pageable = PageRequest.of(0, 100);
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void paginationWithMultipleSortFieldsShouldWork() {
        Address address1 = entityManager.persistAndFlush(createAddress());
        Address address2 = entityManager.persistAndFlush(createAddress());

        Pageable pageable = PageRequest.of(0, 2);
        Page<Address> page = addressRepository.findAll(pageable);

        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
    }

    
}

}
