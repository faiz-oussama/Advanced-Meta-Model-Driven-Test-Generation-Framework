package com.univade.TU.generator.util;

import com.univade.TU.generator.model.AttributeMetaModel;
import com.univade.TU.generator.model.EntityMetaModel;
import com.univade.TU.generator.model.RelationshipMetaModel;


public class TemplateUtils {

    public String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public String camelToSnake(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public String snakeToCamel(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] parts = str.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(capitalize(parts[i]));
        }
        return result.toString();
    }

    public String pluralize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.endsWith("y")) {
            return str.substring(0, str.length() - 1) + "ies";
        }
        if (str.endsWith("s") || str.endsWith("sh") || str.endsWith("ch") || str.endsWith("x") || str.endsWith("z")) {
            return str + "es";
        }
        return str + "s";
    }

    public String getImportStatements(EntityMetaModel entity) {
        StringBuilder imports = new StringBuilder();

        imports.append("import ").append(entity.getFullyQualifiedName()).append(";\n");
        imports.append("import ").append(entity.getPackageName()).append(".repository.").append(entity.getRepositoryName()).append(";\n");

        if (entity.hasRelationships()) {
            for (RelationshipMetaModel rel : entity.getRelationships()) {
                imports.append("import ").append(entity.getPackageName()).append(".").append(rel.getTargetEntity()).append(";\n");
            }
        }

        imports.append("import org.junit.jupiter.api.Test;\n");
        imports.append("import org.junit.jupiter.api.BeforeEach;\n");
        imports.append("import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;\n");
        imports.append("import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;\n");
        imports.append("import org.springframework.beans.factory.annotation.Autowired;\n");
        imports.append("import static org.assertj.core.api.Assertions.*;\n");

        return imports.toString();
    }

    public String getBuilderImports(EntityMetaModel entity) {
        StringBuilder imports = new StringBuilder();

        imports.append("import ").append(entity.getFullyQualifiedName()).append(";\n");

        if (entity.hasRelationships()) {
            for (RelationshipMetaModel rel : entity.getRelationships()) {
                imports.append("import ").append(entity.getPackageName()).append(".").append(rel.getTargetEntity()).append(";\n");
            }
        }

        imports.append("import java.util.*;\n");

        return imports.toString();
    }

    public boolean hasStringAttributes(EntityMetaModel entity) {
        return entity.getAttributes().stream()
                .anyMatch(AttributeMetaModel::isStringType);
    }

    public boolean hasNumericAttributes(EntityMetaModel entity) {
        return entity.getAttributes().stream()
                .anyMatch(AttributeMetaModel::isNumericType);
    }

    public boolean hasDateAttributes(EntityMetaModel entity) {
        return entity.getAttributes().stream()
                .anyMatch(AttributeMetaModel::isDateType);
    }
}
