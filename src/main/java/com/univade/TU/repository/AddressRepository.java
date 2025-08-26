package com.univade.TU.repository;

import com.univade.TU.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCity(String city);
    List<Address> findByStreet(String street);
    List<Address> findByZipCode(String zipCode);
    Optional<Address> findByStreetAndCityAndZipCode(String street, String city, String zipCode);

    List<Address> findByCityContainingIgnoreCase(String city);
    List<Address> findByStreetContainingIgnoreCase(String street);

    @Query("SELECT a FROM Address a WHERE LOWER(a.street) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Address> searchByStreetOrCity(@Param("searchTerm") String searchTerm);

    Page<Address> findByCity(String city, Pageable pageable);
    Page<Address> findByCityContainingIgnoreCase(String city, Pageable pageable);

    List<Address> findAllByOrderByCityAsc();
    List<Address> findAllByOrderByStreetAsc();
    List<Address> findByCityOrderByStreetAsc(String city);

    // existence methods
    boolean existsByCity(String city);
    boolean existsByStreet(String street);
    boolean existsByZipCode(String zipCode);
    boolean existsByStreetAndCity(String street, String city);

    // deletion methods
    long countByCity(String city);
    long countByZipCode(String zipCode);

    void deleteByCity(String city);
    void deleteByZipCode(String zipCode);
}
