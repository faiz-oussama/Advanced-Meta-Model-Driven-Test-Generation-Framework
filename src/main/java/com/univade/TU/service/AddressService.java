package com.univade.TU.service;

import com.univade.TU.entity.Address;
import com.univade.TU.exception.EntityNotFoundException;
import com.univade.TU.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address createAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public Address getAddressById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Address> getAllAddresss() {
        return addressRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Address> getAllAddresss(Pageable pageable) {
        return addressRepository.findAll(pageable);
    }

    public Address updateAddress(Long id, Address address) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        
        Address existingAddress = getAddressById(id);
        existingAddress.setStreet(address.getStreet());
        existingAddress.setCity(address.getCity());
        existingAddress.setZipCode(address.getZipCode());
        
        return addressRepository.save(existingAddress);
    }

    public void deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (!addressRepository.existsById(id)) {
            throw new EntityNotFoundException("Address not found with id: " + id);
        }
        addressRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsAddressById(Long id) {
        if (id == null) {
            return false;
        }
        return addressRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public long countAddresss() {
        return addressRepository.count();
    }

    @Transactional(readOnly = true)
    public List<Address> findAddressByCity(String city) {
        return addressRepository.findByCity(city);
    }

    @Transactional(readOnly = true)
    public List<Address> findAddressByZipCode(String zipCode) {
        return addressRepository.findByZipCode(zipCode);
    }
}
