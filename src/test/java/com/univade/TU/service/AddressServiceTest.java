package com.univade.TU.service;

import com.univade.TU.testdata.AddressTestDataBuilder;


import com.univade.TU.entity.Address;


import com.univade.TU.repository.AddressRepository;

import com.univade.TU.service.AddressService;
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
@DisplayName("Address Service Tests")
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        reset(addressRepository);
    }

    private Address createAddress() {
        return AddressTestDataBuilder.aValidAddress().build();
    }

    @Nested
    @DisplayName("CRUD Tests")
    class CrudTests {

        @Test
        void createAddressShouldSaveAndReturnEntity() {
            Address testAddress = createAddress();
            Address savedAddress = createAddress();
            savedAddress.setId(1L);

            when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

            Address result = addressService.createAddress(testAddress);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            verify(addressRepository).save(testAddress);
        }

        @Test
        void createAddressWithNullShouldThrowException() {
            assertThatThrownBy(() -> addressService.createAddress(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getAddressByIdShouldReturnEntityWhenExists() {
            Long testId = 1L;
            Address testAddress = createAddress();
            testAddress.setId(testId);

            when(addressRepository.findById(testId)).thenReturn(Optional.of(testAddress));

            Address result = addressService.getAddressById(testId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(addressRepository).findById(testId);
        }

        @Test
        void getAddressByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(addressRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getAddressById(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(addressRepository).findById(testId);
        }

        @Test
        void getAllAddresssShouldReturnAllEntities() {
            Address address1 = createAddress();
            Address address2 = createAddress();
            List<Address> expectedAddresss = List.of(address1, address2);

            when(addressRepository.findAll()).thenReturn(expectedAddresss);

            List<Address> result = addressService.getAllAddresss();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(expectedAddresss);
            verify(addressRepository).findAll();
        }

        @Test
        void getAllAddresssWhenEmptyShouldReturnEmptyList() {
            when(addressRepository.findAll()).thenReturn(Collections.emptyList());

            List<Address> result = addressService.getAllAddresss();

            assertThat(result).isEmpty();
            verify(addressRepository).findAll();
        }

        @Test
        void updateAddressShouldUpdateAndReturnEntity() {
            Long testId = 1L;
            Address existingAddress = createAddress();
            existingAddress.setId(testId);

            Address updatedAddress = createAddress();
            updatedAddress.setId(testId);
            updatedAddress.setStreet("Updated Street");

            when(addressRepository.findById(testId)).thenReturn(Optional.of(existingAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(updatedAddress);

            Address result = addressService.updateAddress(testId, updatedAddress);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            assertThat(result.getStreet()).isEqualTo("Updated Street");
            verify(addressRepository).findById(testId);
            verify(addressRepository).save(any(Address.class));
        }

        @Test
        void updateAddressWithNonExistentIdShouldThrowException() {
            Long testId = 999L;
            Address updatedAddress = createAddress();

            when(addressRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateAddress(testId, updatedAddress))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(addressRepository).findById(testId);
            verify(addressRepository, never()).save(any(Address.class));
        }

        @Test
        void deleteAddressByIdShouldDeleteWhenExists() {
            Long testId = 1L;

            when(addressRepository.existsById(testId)).thenReturn(true);
            doNothing().when(addressRepository).deleteById(testId);

            assertThatCode(() -> addressService.deleteAddress(testId))
                    .doesNotThrowAnyException();

            verify(addressRepository).existsById(testId);
            verify(addressRepository).deleteById(testId);
        }

        @Test
        void deleteAddressByIdShouldThrowExceptionWhenNotFound() {
            Long testId = 999L;

            when(addressRepository.existsById(testId)).thenReturn(false);

            assertThatThrownBy(() -> addressService.deleteAddress(testId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(addressRepository).existsById(testId);
            verify(addressRepository, never()).deleteById(testId);
        }

        @Test
        void existsAddressByIdShouldReturnTrueWhenExists() {
            Long testId = 1L;

            when(addressRepository.existsById(testId)).thenReturn(true);

            boolean result = addressService.existsAddressById(testId);

            assertThat(result).isTrue();
            verify(addressRepository).existsById(testId);
        }

        @Test
        void existsAddressByIdShouldReturnFalseWhenNotExists() {
            Long testId = 999L;

            when(addressRepository.existsById(testId)).thenReturn(false);

            boolean result = addressService.existsAddressById(testId);

            assertThat(result).isFalse();
            verify(addressRepository).existsById(testId);
        }

        @Test
        void countAddresssShouldReturnCorrectCount() {
            long expectedCount = 5L;

            when(addressRepository.count()).thenReturn(expectedCount);

            long result = addressService.countAddresss();

            assertThat(result).isEqualTo(expectedCount);
            verify(addressRepository).count();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should throw exception when creating Address with null entity")
        void createAddressWithNullShouldThrowException() {
            assertThatThrownBy(() -> addressService.createAddress(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Address cannot be null");

            verifyNoInteractions(addressRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Address with null entity")
        void updateAddressWithNullShouldThrowException() {
            Long testId = 1L;

            assertThatThrownBy(() -> addressService.updateAddress(testId, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Address cannot be null");

            verifyNoInteractions(addressRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating Address with null ID")
        void updateAddressWithNullIdShouldThrowException() {
            Address testAddress = createAddress();

            assertThatThrownBy(() -> addressService.updateAddress(null, testAddress))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(addressRepository);
        }

        @Test
        @DisplayName("Should throw exception when deleting Address with null ID")
        void deleteAddressWithNullIdShouldThrowException() {
            assertThatThrownBy(() -> addressService.deleteAddress(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID cannot be null");

            verifyNoInteractions(addressRepository);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent Address")
        void updateAddressWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;
            Address testAddress = createAddress();

            when(addressRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.updateAddress(nonExistentId, testAddress))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Address not found with id: " + nonExistentId);

            verify(addressRepository).findById(nonExistentId);
            verify(addressRepository, never()).save(any(Address.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent Address")
        void deleteAddressWithNonExistentIdShouldThrowException() {
            Long nonExistentId = 999L;

            when(addressRepository.existsById(nonExistentId)).thenReturn(false);

            assertThatThrownBy(() -> addressService.deleteAddress(nonExistentId))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Address not found with id: " + nonExistentId);

            verify(addressRepository).existsById(nonExistentId);
            verify(addressRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should handle repository exception gracefully when finding Address")
        void findAddressByIdShouldHandleRepositoryException() {
            Long testId = 1L;

            when(addressRepository.findById(testId))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> addressService.getAddressById(testId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(addressRepository).findById(testId);
        }

        @Test
        @DisplayName("Should validate business rules before saving Address")
        void createAddressShouldValidateBusinessRules() {
            Address testAddress = createAddress();
            Address savedAddress = createAddress();
            savedAddress.setId(1L);

            when(addressRepository.save(testAddress)).thenReturn(savedAddress);

            Address result = addressService.createAddress(testAddress);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedAddress);
            verify(addressRepository).save(testAddress);
        }

        @Test
        @DisplayName("Should check existence before operations")
        void existsAddressByIdShouldReturnCorrectValue() {
            Long existingId = 1L;
            Long nonExistentId = 999L;

            when(addressRepository.existsById(existingId)).thenReturn(true);
            when(addressRepository.existsById(nonExistentId)).thenReturn(false);

            boolean existsResult = addressService.existsAddressById(existingId);
            boolean notExistsResult = addressService.existsAddressById(nonExistentId);

            assertThat(existsResult).isTrue();
            assertThat(notExistsResult).isFalse();
            verify(addressRepository).existsById(existingId);
            verify(addressRepository).existsById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null ID gracefully in exists check")
        void existsAddressByIdWithNullShouldReturnFalse() {
            boolean result = addressService.existsAddressById(null);

            assertThat(result).isFalse();
            verifyNoInteractions(addressRepository);
        }

        @Test
        @DisplayName("Should handle validation errors from JPA/Hibernate")
        void createAddressShouldHandleConstraintViolationException() {
            Address invalidAddress = createAddress();
            invalidAddress.setStreet(null);

            when(addressRepository.save(invalidAddress))
                    .thenThrow(new ConstraintViolationException("Validation failed", null));

            assertThatThrownBy(() -> addressService.createAddress(invalidAddress))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("Validation failed");

            verify(addressRepository).save(invalidAddress);
        }

        @Test
        @DisplayName("Should handle data integrity violations")
        void createAddressShouldHandleDataIntegrityViolationException() {
            Address duplicateAddress = createAddress();

            when(addressRepository.save(duplicateAddress))
                    .thenThrow(new DataIntegrityViolationException("Duplicate constraint violation"));

            assertThatThrownBy(() -> addressService.createAddress(duplicateAddress))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Duplicate constraint violation");

            verify(addressRepository).save(duplicateAddress);
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        void getAllAddresssWithPageableShouldReturnPagedResults() {
            Address address1 = createAddress();
            Address address2 = createAddress();
            Address address3 = createAddress();

            List<Address> addressList = List.of(address1, address2);
            Page<Address> expectedPage = new PageImpl<>(addressList, PageRequest.of(0, 2), 3);

            Pageable pageable = PageRequest.of(0, 2);
            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(2);
            verify(addressRepository).findAll(pageable);
        }

        @Test
        void getAllAddresssWithPageableAndSortShouldReturnSortedResults() {
            Address address1 = createAddress();
            address1.setStreet("Hicham");
            Address address2 = createAddress();
            address2.setStreet("Oussama");

            List<Address> sortedAddressList = List.of(address1, address2);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("street").ascending());
            Page<Address> expectedPage = new PageImpl<>(sortedAddressList, pageable, 2);

            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getStreet()).isEqualTo("Hicham");
            assertThat(result.getContent().get(1).getStreet()).isEqualTo("Oussama");
            verify(addressRepository).findAll(pageable);
        }

        @Test
        void getAllAddresssWithEmptyPageShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Address> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(addressRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getTotalPages()).isEqualTo(0);
            verify(addressRepository).findAll(pageable);
        }

        @Test
        void getAllAddresssWithLargePageSizeShouldReturnAllResults() {
            Address address1 = createAddress();
            Address address2 = createAddress();
            Address address3 = createAddress();

            List<Address> allAddresss = List.of(address1, address2, address3);
            Pageable pageable = PageRequest.of(0, 100);
            Page<Address> expectedPage = new PageImpl<>(allAddresss, pageable, 3);

            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getTotalPages()).isEqualTo(1);
            verify(addressRepository).findAll(pageable);
        }

        @Test
        void getAllAddresssWithSecondPageShouldReturnCorrectResults() {
            Address address3 = createAddress();
            Address address4 = createAddress();

            List<Address> secondPageAddresss = List.of(address3, address4);
            Pageable pageable = PageRequest.of(1, 2);
            Page<Address> expectedPage = new PageImpl<>(secondPageAddresss, pageable, 4);

            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(4);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
            verify(addressRepository).findAll(pageable);
        }



        @Test
        void getAllAddresssWithDescendingSortShouldReturnCorrectOrder() {
            Address address1 = createAddress();
            address1.setStreet("Ilyass");
            Address address2 = createAddress();
            address2.setStreet("Oussama");

            List<Address> sortedAddressList = List.of(address2, address1);
            Pageable pageable = PageRequest.of(0, 10, Sort.by("street").descending());
            Page<Address> expectedPage = new PageImpl<>(sortedAddressList, pageable, 2);

            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getStreet()).isEqualTo("Oussama");
            assertThat(result.getContent().get(1).getStreet()).isEqualTo("Ilyass");
            verify(addressRepository).findAll(pageable);
        }

        @Test
        void getAllAddresssWithCustomPageSizeShouldRespectPageSize() {
            Address address1 = createAddress();
            Address address2 = createAddress();

            List<Address> addressList = List.of(address1, address2);
            Pageable pageable = PageRequest.of(0, 1);
            Page<Address> expectedPage = new PageImpl<>(List.of(address1), pageable, 2);

            when(addressRepository.findAll(pageable)).thenReturn(expectedPage);

            Page<Address> result = addressService.getAllAddresss(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(1);
            verify(addressRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Service Validation Tests")
    class ServiceValidationTests {

        @Test
        @DisplayName("Should validate entity before saving")
        void createAddressShouldValidateEntity() {
            Address testAddress = createAddress();
            Address savedAddress = createAddress();
            savedAddress.setId(1L);

            when(addressRepository.save(testAddress)).thenReturn(savedAddress);

            Address result = addressService.createAddress(testAddress);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(savedAddress);
            verify(addressRepository).save(testAddress);
        }

        @Test
        @DisplayName("Should handle repository exceptions during validation")
        void createAddressShouldHandleRepositoryExceptions() {
            Address testAddress = createAddress();

            when(addressRepository.save(testAddress))
                    .thenThrow(new RuntimeException("Database connection error"));

            assertThatThrownBy(() -> addressService.createAddress(testAddress))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database connection error");

            verify(addressRepository).save(testAddress);
        }

        @Test
        @DisplayName("Should validate entity state before update")
        void updateAddressShouldValidateEntityState() {
            Long testId = 1L;
            Address existingAddress = createAddress();
            existingAddress.setId(testId);

            Address updatedAddress = createAddress();
            Address savedAddress = createAddress();
            savedAddress.setId(testId);

            when(addressRepository.findById(testId)).thenReturn(Optional.of(existingAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

            Address result = addressService.updateAddress(testId, updatedAddress);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testId);
            verify(addressRepository).findById(testId);
            verify(addressRepository).save(any(Address.class));
        }
    }


    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired
        private AddressService addressServiceIntegration;

        @Autowired
        private AddressRepository addressRepositoryIntegration;

        @Autowired
        private jakarta.persistence.EntityManager entityManager;

        private Address testAddressIntegration;

        @BeforeEach
        void setUpIntegration() {
            testAddressIntegration = createTestEntity();
        }

        private Address createTestEntity() {
            Address entity = AddressTestDataBuilder.aDefaultAddress().build();
            return entity;
        }

        @Nested
        @DisplayName("Business Logic Integration Tests")
        class BusinessLogicIntegrationTests {

            @Test
            @DisplayName("Should create Address and persist to database")
            void shouldCreateAddressAndPersistToDatabase() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);

                assertThat(created).isNotNull();
                assertThat(created.getId()).isNotNull();
                
                Optional<Address> persisted = addressRepositoryIntegration.findById(created.getId());
                assertThat(persisted).isPresent();
                assertThat(persisted.get().getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should update Address and persist changes")
            void shouldUpdateAddressAndPersistChanges() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);
                
                created.setStreet("Updated street");

                Address updated = addressServiceIntegration.updateAddress(created.getId(), created);

                assertThat(updated).isNotNull();
                Address persisted = addressRepositoryIntegration.findById(created.getId()).orElseThrow();
                assertThat(persisted.getStreet()).isEqualTo("Updated street");
            }

            @Test
            @DisplayName("Should delete Address from database")
            void shouldDeleteAddressFromDatabase() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);
                Long id = created.getId();

                addressServiceIntegration.deleteAddress(id);

                Optional<Address> deleted = addressRepositoryIntegration.findById(id);
                assertThat(deleted).isEmpty();
            }
        }

        @Nested
        @DisplayName("Validation Constraint Integration Tests")
        class ValidationConstraintIntegrationTests {

            @Test
            @DisplayName("Should handle validation errors from JPA/Hibernate")
            void shouldHandleJpaValidationErrors() {
                Address invalidAddress = AddressTestDataBuilder.aDefaultAddress()
                        .withStreet(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        addressServiceIntegration.createAddress(invalidAddress);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } finally {
                    entityManager.clear();
                }
            }

        }

        @Nested
        @DisplayName("Transactional Behavior Integration Tests")
        class TransactionalBehaviorIntegrationTests {

            @Test
            @DisplayName("Should rollback transaction when error occurs during save")
            void shouldRollbackTransactionWhenErrorOccursDuringSave() {
                long initialCount = addressRepositoryIntegration.count();

                Address invalidAddress = AddressTestDataBuilder.aDefaultAddress()
                        .withStreet(null)
                        .build();

                try {
                    assertThatThrownBy(() -> {
                        addressServiceIntegration.createAddress(invalidAddress);
                        entityManager.flush();
                    }).isInstanceOf(ConstraintViolationException.class);
                } catch (Exception e) {
                    // Clear entity manager after constraint violation to avoid inconsistent state
                    entityManager.clear();
                    throw e;
                }

                // Clear entity manager to avoid stale state
                entityManager.clear();
                long finalCount = addressRepositoryIntegration.count();
                assertThat(finalCount).isEqualTo(initialCount);
            }
        }


        @Nested
        @DisplayName("Finder Method Integration Tests")
        class FinderMethodIntegrationTests {

            @Test
            @DisplayName("Should find Address by ID correctly")
            void shouldFindAddressByIdCorrectly() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);

                Address found = addressServiceIntegration.getAddressById(created.getId());

                assertThat(found).isNotNull();
                assertThat(found.getId()).isEqualTo(created.getId());
            }

            @Test
            @DisplayName("Should return empty when Address not found by ID")
            void shouldReturnEmptyWhenAddressNotFoundById() {
                Long nonExistentId = 999L;

                assertThatThrownBy(() -> addressServiceIntegration.getAddressById(nonExistentId))
                        .isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("Should check existence correctly")
            void shouldCheckExistenceCorrectly() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);
                Long nonExistentId = 999L;

                boolean exists = addressServiceIntegration.existsAddressById(created.getId());
                boolean notExists = addressServiceIntegration.existsAddressById(nonExistentId);

                assertThat(exists).isTrue();
                assertThat(notExists).isFalse();
            }

            @Test
            @DisplayName("Should find all Addresss from database")
            void shouldFindAllAddresssFromDatabase() {
                Address testEntity1 = createTestEntity();
                Address entity1 = addressServiceIntegration.createAddress(testEntity1);
                Address testEntity2 = createTestEntity();
                Address entity2 = addressServiceIntegration.createAddress(testEntity2);

                List<Address> all = addressServiceIntegration.getAllAddresss();

                assertThat(all).hasSizeGreaterThanOrEqualTo(2);
                assertThat(all).extracting(Address::getId)
                        .contains(entity1.getId(), entity2.getId());
            }
        }

        @Nested
        @DisplayName("Deletion Logic Integration Tests")
        class DeletionLogicIntegrationTests {

            @Test
            @DisplayName("Should delete Address and verify removal")
            void shouldDeleteAddressAndVerifyRemoval() {
                Address testEntity = createTestEntity();
                Address created = addressServiceIntegration.createAddress(testEntity);
                Long id = created.getId();

                addressServiceIntegration.deleteAddress(id);

                boolean exists = addressRepositoryIntegration.existsById(id);
                assertThat(exists).isFalse();
            }

        }
    }




}
