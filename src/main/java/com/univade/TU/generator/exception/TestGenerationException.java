package com.univade.TU.generator.exception;

public class TestGenerationException extends RuntimeException {

    public TestGenerationException(String message) {
        super(message);
    }

    public TestGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestGenerationException(Throwable cause) {
        super(cause);
    }

    public static TestGenerationException templateProcessingError(String templateName, Throwable cause) {
        return new TestGenerationException("Error processing template: " + templateName, cause);
    }

    public static TestGenerationException jsonParsingError(String json, Throwable cause) {
        return new TestGenerationException("Error parsing JSON meta-model", cause);
    }

    public static TestGenerationException annotationParsingError(Class<?> entityClass, Throwable cause) {
        return new TestGenerationException("Error parsing annotations for entity: " + entityClass.getSimpleName(), cause);
    }

    public static TestGenerationException templateNotFound(String templateName) {
        return new TestGenerationException("Template not found: " + templateName);
    }

    public static TestGenerationException invalidMetaModel(String reason) {
        return new TestGenerationException("Invalid meta-model: " + reason);
    }

    public static TestGenerationException fileWriteError(String fileName, Throwable cause) {
        return new TestGenerationException("Error writing file: " + fileName, cause);
    }
}
