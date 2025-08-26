package com.univade.TU.controller;

import com.univade.TU.dto.AddressDto;
import com.univade.TU.entity.Address;
import com.univade.TU.exception.BadRequestException;
import com.univade.TU.service.AddressService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@Validated
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AddressDto> createAddress(@Valid @RequestBody AddressDto addressDto) {
        if (addressDto == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        Address address = convertToEntity(addressDto);
        Address createdAddress = addressService.createAddress(address);
        AddressDto responseDto = convertToDto(createdAddress);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAddress.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Long id) {
        if (id <= 0) {
            throw new BadRequestException("ID must be positive");
        }
        Address address = addressService.getAddressById(id);
        AddressDto addressDto = convertToDto(address);
        return ResponseEntity.ok(addressDto);
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AddressDto>> getAllAddresses() {
        List<Address> addresses = addressService.getAllAddresss();
        List<AddressDto> addressDtos = addresses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addressDtos);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<AddressDto>> getAllAddresses(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (pageable.getPageSize() > 1000) {
            throw new BadRequestException("Page size cannot exceed 1000");
        }

        Page<Address> addresses = addressService.getAllAddresss(pageable);
        Page<AddressDto> addressDtos = addresses.map(this::convertToDto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(addressDtos.getTotalElements()))
                .body(addressDtos);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddressDto> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDto addressDto) {
        Address address = convertToEntity(addressDto);
        Address updatedAddress = addressService.updateAddress(id, address);
        AddressDto responseDto = convertToDto(updatedAddress);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> existsAddressById(@PathVariable Long id) {
        boolean exists = addressService.existsAddressById(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countAddresses() {
        long count = addressService.countAddresss();
        return ResponseEntity.ok(count);
    }

    private AddressDto convertToDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .zipCode(address.getZipCode())
                .build();
    }

    private Address convertToEntity(AddressDto addressDto) {
        Address address = new Address();
        address.setId(addressDto.getId());
        address.setStreet(addressDto.getStreet());
        address.setCity(addressDto.getCity());
        address.setZipCode(addressDto.getZipCode());
        return address;
    }
}
