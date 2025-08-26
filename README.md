# TU - Advanced Meta-Model Driven Test Generation Framework

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![FreeMarker](https://img.shields.io/badge/FreeMarker-2.3+-red.svg)](https://freemarker.apache.org/)

## 🚀 Overview

**TU (Testing Univade)** is a sophisticated, enterprise-grade meta-model driven test generation framework that revolutionizes automated testing for Spring Boot applications. Built with advanced architectural patterns and leveraging cutting-edge template engineering, TU automatically generates comprehensive test suites across all application layers - Repository, Service, and Controller - with unprecedented precision and coverage.

## 🏗️ Advanced Architecture

### Meta-Model Driven Design
The framework employs a sophisticated meta-model architecture that abstracts entity definitions into rich, queryable data structures:

- **EntityMetaModel**: Core abstraction containing entity metadata, attributes, relationships, validations, and security rules
- **AttributeMetaModel**: Advanced attribute modeling with type inference, constraint validation, and database mapping
- **RelationshipMetaModel**: Complex relationship handling supporting OneToOne, OneToMany, ManyToOne, and ManyToMany associations
- **ValidationMetaModel**: Comprehensive validation rule modeling with Jakarta Bean Validation integration
- **SecurityRuleMetaModel**: Role-based access control modeling with HTTP method-specific permissions

### Multi-Layer Test Generation Engine

```
┌─────────────────────────────────────────────────────────────┐
│                    TU Framework Architecture                │
├─────────────────────────────────────────────────────────────┤
│  JSON/Annotation Parser → EntityMetaModel → Test Generators │
│                                                             │
│  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────┐ │
│  │ Repository      │  │ Service          │  │ Controller  │ │
│  │ Test Generator  │  │ Test Generator   │  │ Test Gen.   │ │
│  │                 │  │                  │  │             │ │
│  │ • CRUD Tests    │  │ • Unit Tests     │  │ • REST API  │ │
│  │ • Validation    │  │ • Integration    │  │ • Security  │ │
│  │ • Constraints   │  │ • Business Logic │  │ • Validation│ │
│  │ • Relationships │  │ • Transactions   │  │ • HTTP      │ │
│  │ • Pagination    │  │ • Exception      │  │ • JSON      │ │
│  └─────────────────┘  └──────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 Core Technical Components

### 1. EntityMetaModelParser
Advanced dual-mode parser supporting:
- **JSON-based parsing**: Declarative entity definition through structured JSON
- **Annotation-based parsing**: Runtime reflection-based entity introspection
- **Hybrid validation**: Cross-validation between JSON schema and JPA annotations

### 2. Template Engine Integration
Leverages Apache FreeMarker for sophisticated template processing:
- **Modular template architecture**: Composable template includes for different test aspects
- **Dynamic template resolution**: Context-aware template selection based on entity features
- **Advanced macro system**: Reusable template components with parameter injection

### 3. Multi-Generator Architecture

#### RepositoryTestGenerator
- **Database constraint testing**: Nullable, unique, length, and range validations
- **Transaction rollback verification**: Ensures data consistency on constraint violations
- **Relationship integrity testing**: Foreign key constraints and cascade operations
- **Pagination and sorting validation**: Spring Data JPA query method testing

#### ServiceTestGenerator  
- **Dual testing approach**: Unit tests with Mockito and integration tests with Spring context
- **Business logic validation**: Use case-oriented testing scenarios
- **Transaction boundary testing**: @Transactional behavior verification
- **Exception handling coverage**: Comprehensive error scenario testing

#### ControllerTestGenerator
- **REST API endpoint testing**: Full HTTP method coverage (GET, POST, PUT, DELETE)
- **Security integration testing**: Role-based access control with @WithMockUser
- **Request/Response validation**: JSON structure and content verification
- **Error response testing**: HTTP status code and error message validation

## 🛡️ Advanced Security Testing

### Role-Based Access Control Testing
```json
{
  "securityRules": [
    {
      "path": "/api/users",
      "methods": {
        "GET": ["USER", "ADMIN"],
        "POST": ["ADMIN"]
      }
    }
  ]
}
```

The framework automatically generates comprehensive security tests:
- **Authentication scenarios**: Authenticated vs unauthenticated access
- **Authorization matrix testing**: All role-endpoint combinations
- **Security configuration isolation**: Test-specific security contexts
- **Dynamic user creation**: Role-based test user generation

## 📊 Intelligent Test Data Generation

### TestDataGenerator Utility
Advanced test data generation with:
- **Type-aware value generation**: Context-sensitive data creation
- **Constraint-compliant data**: Respects validation rules and database constraints
- **Relationship-aware generation**: Maintains referential integrity
- **Moroccan localization**: Culturally relevant test data (Oussama, Hicham, Ilyass, Mohammed)

### Builder Pattern Implementation
```java
public static UserTestDataBuilder aDefaultUser() {
    return new UserTestDataBuilder()
            .withName("Oussama")
            .withEmail("oussama@univade.com")
            .withAge(25);
}
```

## 🔍 Validation Engine

### Multi-Layer Validation Testing
1. **Database-level constraints**: NOT_NULL, UNIQUE, LENGTH, RANGE
2. **Bean validation annotations**: @NotBlank, @Email, @Size, @Pattern
3. **Custom validation rules**: Business-specific validation logic
4. **Cross-field validation**: Complex validation scenarios

### Dynamic Validation Rule Detection
The framework intelligently detects and tests validation rules:
```java
private List<DatabaseValidationRule> buildValidationRules(EntityMetaModel entity) {
    return entity.getAttributes().stream()
        .filter(attr -> hasValidationConstraints(attr))
        .map(this::createValidationRule)
        .collect(Collectors.toList());
}
```

## 🚀 Template-Driven Code Generation

### FreeMarker Template Architecture
```
templates/
├── repository/
│   ├── repository-test.ftl
│   ├── crud-tests.ftl
│   ├── validation-tests.ftl
│   └── relationship-tests.ftl
├── service/
│   ├── service-test.ftl
│   ├── service-crud-tests.ftl
│   └── service-integration-tests.ftl
└── controller/
    ├── controller-test.ftl
    ├── controller-security-tests.ftl
    └── controller-validation-tests.ftl
```

### Conditional Template Inclusion
```freemarker
<#if entity.hasValidations()>
    <#include "validation-tests.ftl">
</#if>

<#if entity.hasRelationships()>
    <#include "relationship-tests.ftl">
</#if>

<#if entity.hasSecurityRules()>
    <#include "security-tests.ftl">
</#if>
```

## 🔧 Configuration & Setup

### Maven Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-freemarker</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>
```

### Application Configuration
```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Logging Configuration
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql=TRACE
```

## 📈 Usage Examples

### JSON-Based Entity Definition
```java
@Test
void generateTestsFromJson() {
    String jsonMetaModel = """
        {
            "name": "User",
            "packageName": "com.univade.TU",
            "attributes": [
                {
                    "name": "email",
                    "type": "String",
                    "unique": true,
                    "email": true,
                    "notBlank": true
                }
            ]
        }
        """;
    
    GeneratedTestResult result = repositoryTestGenerator.generateFromJson(jsonMetaModel);
    fileWriterService.writeTestFiles(result);
}
```

### Annotation-Based Generation
```java
@Test
void generateTestsFromAnnotations() {
    GeneratedTestResult result = serviceTestGenerator.generateFromAnnotations(User.class);
    fileWriterService.writeTestFiles(result);
}
```

## 🎯 Advanced Features

### 1. **Entity-Agnostic Design**
- Framework works with any entity type without modification
- Dynamic adaptation to entity structure and constraints
- Scalable architecture supporting complex domain models

### 2. **Comprehensive Test Coverage**
- **Repository Layer**: Database constraints, CRUD operations, relationships
- **Service Layer**: Business logic, transactions, exception handling
- **Controller Layer**: REST endpoints, security, validation, HTTP protocols

### 3. **Intelligent Template Processing**
- Context-aware template selection
- Dynamic import generation based on entity metadata
- Conditional test generation based on entity features

### 4. **Production-Ready Quality**
- Clean, human-readable generated code
- No AI-generated comments or markers
- Industry best practices and coding standards

## 🔬 Testing Strategy

The framework implements a sophisticated testing approach:

1. **Nested Test Structure**: Organized test classes with `@Nested` annotations
2. **Builder Pattern**: Consistent test data creation across all layers
3. **Transaction Testing**: Rollback verification for data consistency
4. **Security Integration**: Comprehensive role-based access testing
5. **Edge Case Coverage**: Boundary conditions and error scenarios

## 🚀 Getting Started

1. **Clone the repository**
2. **Configure your entities** using JSON meta-models or JPA annotations
3. **Run the test generators** to create comprehensive test suites
4. **Execute generated tests** to validate your application layers

## 🏆 Benefits

- **Reduced Development Time**: Automated test generation saves hours of manual coding
- **Consistent Quality**: Standardized test patterns across all entities
- **Comprehensive Coverage**: Multi-layer testing ensures robust applications
- **Maintainable Code**: Clean, readable generated tests
- **Security Assurance**: Built-in security testing for all endpoints
- **Scalable Architecture**: Easily extensible for new entity types and test scenarios

---

*TU Framework - Revolutionizing Enterprise Test Automation*
