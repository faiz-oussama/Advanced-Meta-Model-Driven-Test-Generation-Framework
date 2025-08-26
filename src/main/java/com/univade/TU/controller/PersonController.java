package com.univade.TU.controller;

import com.univade.TU.dto.PersonDto;
import com.univade.TU.entity.Person;
import com.univade.TU.exception.BadRequestException;
import com.univade.TU.service.PersonService;
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
@RequestMapping("/api/persons")
@Validated
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PersonDto> createPerson(@Valid @RequestBody PersonDto personDto) {
        if (personDto == null) {
            throw new BadRequestException("Request body cannot be null");
        }
        Person person = convertToEntity(personDto);
        Person createdPerson = personService.createPerson(person);
        PersonDto responseDto = convertToDto(createdPerson);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{cin}")
                .buildAndExpand(createdPerson.getCin())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping(value = "/{cin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDto> getPersonById(@PathVariable String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            throw new BadRequestException("CIN cannot be null or empty");
        }
        Person person = personService.getPersonById(cin);
        PersonDto personDto = convertToDto(person);
        return ResponseEntity.ok(personDto);
    }

    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PersonDto>> getAllPersons() {
        List<Person> persons = personService.getAllPersons();
        List<PersonDto> personDtos = persons.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(personDtos);
    }

    @GetMapping(value = "/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PersonDto>> getAllPersons(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException("Page number must be non-negative");
        }
        if (pageable.getPageSize() <= 0) {
            throw new BadRequestException("Page size must be positive");
        }
        if (pageable.getPageSize() > 1000) {
            throw new BadRequestException("Page size cannot exceed 1000");
        }

        Page<Person> persons = personService.getAllPersons(pageable);
        Page<PersonDto> personDtos = persons.map(this::convertToDto);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(personDtos.getTotalElements()))
                .body(personDtos);
    }

    @PutMapping(value = "/{cin}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PersonDto> updatePerson(@PathVariable String cin, @Valid @RequestBody PersonDto personDto) {
        Person person = convertToEntity(personDto);
        Person updatedPerson = personService.updatePerson(cin, person);
        PersonDto responseDto = convertToDto(updatedPerson);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{cin}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deletePerson(@PathVariable String cin) {
        personService.deletePerson(cin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{cin}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> existsPersonById(@PathVariable String cin) {
        boolean exists = personService.existsPersonById(cin);
        return ResponseEntity.ok(exists);
    }

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countPersons() {
        long count = personService.countPersons();
        return ResponseEntity.ok(count);
    }

    private PersonDto convertToDto(Person person) {
        return PersonDto.builder()
                .cin(person.getCin())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .phoneNumber(person.getPhoneNumber())
                .dateOfBirth(person.getDateOfBirth())
                .build();
    }

    private Person convertToEntity(PersonDto personDto) {
        Person person = new Person();
        person.setCin(personDto.getCin());
        person.setFirstName(personDto.getFirstName());
        person.setLastName(personDto.getLastName());
        person.setEmail(personDto.getEmail());
        person.setPhoneNumber(personDto.getPhoneNumber());
        person.setDateOfBirth(personDto.getDateOfBirth());
        return person;
    }
}
