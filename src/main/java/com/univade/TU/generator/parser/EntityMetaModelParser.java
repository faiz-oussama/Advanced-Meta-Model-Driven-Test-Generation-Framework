package com.univade.TU.generator.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.univade.TU.generator.exception.TestGenerationException;
import com.univade.TU.generator.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EntityMetaModelParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public EntityMetaModel parseFromJson(String jsonMetaModel) {
        try {
            return mapper.readValue(jsonMetaModel, EntityMetaModel.class);
        } catch (IOException e) {
            throw TestGenerationException.jsonParsingError(jsonMetaModel, e);
        }
    }

    public EntityMetaModel parseFromAnnotations(Class<?> entityClass) {
        try {
            EntityMetaModel.EntityMetaModelBuilder builder = EntityMetaModel.builder();
            
            builder.name(entityClass.getSimpleName());
            builder.packageName(entityClass.getPackage().getName());
            
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (tableAnnotation != null) {
                builder.tableName(tableAnnotation.name());
            }
            
            List<AttributeMetaModel> attributes = new ArrayList<>();
            List<RelationshipMetaModel> relationships = new ArrayList<>();
            List<ValidationMetaModel> validations = new ArrayList<>();
            
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (isRelationshipField(field)) {
                    relationships.add(parseRelationship(field));
                } else {
                    AttributeMetaModel attribute = parseAttribute(field);
                    attributes.add(attribute);
                    validations.addAll(parseValidations(field));
                }
            }
            
            builder.attributes(attributes);
            builder.relationships(relationships);
            builder.validations(validations);
            
            return builder.build();
            
        } catch (Exception e) {
            throw TestGenerationException.annotationParsingError(entityClass, e);
        }
    }

    private AttributeMetaModel parseAttribute(Field field) {
        AttributeMetaModel.AttributeMetaModelBuilder builder = AttributeMetaModel.builder();
        
        builder.name(field.getName());
        builder.type(getJavaTypeString(field.getType()));
        
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            builder.columnName(columnAnnotation.name());
            builder.nullable(columnAnnotation.nullable());
            builder.unique(columnAnnotation.unique());
            if (columnAnnotation.length() > 0) {
                builder.maxLength(columnAnnotation.length());
            }
        }
        
        Id idAnnotation = field.getAnnotation(Id.class);
        if (idAnnotation != null) {
            builder.primaryKey(true);
        }
        
        GeneratedValue generatedValueAnnotation = field.getAnnotation(GeneratedValue.class);
        if (generatedValueAnnotation != null) {
            builder.generatedValue(true);
            builder.generationType(generatedValueAnnotation.strategy().name());
        }
        
        Lob lobAnnotation = field.getAnnotation(Lob.class);
        if (lobAnnotation != null) {
            builder.lob(true);
        }

        Email emailAnnotation = field.getAnnotation(Email.class);
        if (emailAnnotation != null) {
            builder.email(true);
        }

        return builder.build();
    }

    private RelationshipMetaModel parseRelationship(Field field) {
        RelationshipMetaModel.RelationshipMetaModelBuilder builder = RelationshipMetaModel.builder();
        
        builder.name(field.getName());
        builder.targetEntity(getTargetEntityName(field));
        
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        if (oneToOne != null) {
            builder.type("OneToOne");
            builder.mappedBy(oneToOne.mappedBy());
            builder.cascadeType(Arrays.toString(oneToOne.cascade()));
            builder.fetchType(oneToOne.fetch().name());
            builder.optional(oneToOne.optional());
        }
        
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        if (oneToMany != null) {
            builder.type("OneToMany");
            builder.mappedBy(oneToMany.mappedBy());
            builder.cascadeType(Arrays.toString(oneToMany.cascade()));
            builder.fetchType(oneToMany.fetch().name());
            builder.orphanRemoval(oneToMany.orphanRemoval());
        }
        
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        if (manyToOne != null) {
            builder.type("ManyToOne");
            builder.cascadeType(Arrays.toString(manyToOne.cascade()));
            builder.fetchType(manyToOne.fetch().name());
            builder.optional(manyToOne.optional());
        }
        
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        if (manyToMany != null) {
            builder.type("ManyToMany");
            builder.mappedBy(manyToMany.mappedBy());
            builder.cascadeType(Arrays.toString(manyToMany.cascade()));
            builder.fetchType(manyToMany.fetch().name());
        }
        
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (joinColumn != null) {
            builder.joinColumn(joinColumn.name());
        }
        
        return builder.build();
    }

    private List<ValidationMetaModel> parseValidations(Field field) {
        List<ValidationMetaModel> validations = new ArrayList<>();
        
        NotNull notNull = field.getAnnotation(NotNull.class);
        if (notNull != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("NotNull")
                    .message(notNull.message())
                    .build());
        }
        
        NotBlank notBlank = field.getAnnotation(NotBlank.class);
        if (notBlank != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("NotBlank")
                    .message(notBlank.message())
                    .build());
        }
        
        Size size = field.getAnnotation(Size.class);
        if (size != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("Size")
                    .min(size.min())
                    .max(size.max())
                    .message(size.message())
                    .build());
        }
        
        Min min = field.getAnnotation(Min.class);
        if (min != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("Min")
                    .value(min.value())
                    .message(min.message())
                    .build());
        }
        
        Max max = field.getAnnotation(Max.class);
        if (max != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("Max")
                    .value(max.value())
                    .message(max.message())
                    .build());
        }
        
        Email email = field.getAnnotation(Email.class);
        if (email != null) {
            validations.add(ValidationMetaModel.builder()
                    .attributeName(field.getName())
                    .validationType("Email")
                    .message(email.message())
                    .build());
        }
        
        return validations;
    }

    private boolean isRelationshipField(Field field) {
        return field.getAnnotation(OneToOne.class) != null ||
               field.getAnnotation(OneToMany.class) != null ||
               field.getAnnotation(ManyToOne.class) != null ||
               field.getAnnotation(ManyToMany.class) != null;
    }

    public List<ValidationMetaModel> parseValidationsFromDtoClass(Class<?> dtoClass) {
        List<ValidationMetaModel> validations = new ArrayList<>();

        Field[] fields = dtoClass.getDeclaredFields();
        for (Field field : fields) {
            validations.addAll(parseValidations(field));
        }

        return validations;
    }

    private String getTargetEntityName(Field field) {
        Class<?> fieldType = field.getType();
        if (List.class.isAssignableFrom(fieldType)) {
            String genericTypeName = field.getGenericType().getTypeName();
            String fullClassName = genericTypeName.replaceAll(".*<(.*)>.*", "$1");
            return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        }
        return fieldType.getSimpleName();
    }

    private String getJavaTypeString(Class<?> type) {
        if (type == String.class) return "String";
        if (type == Integer.class || type == int.class) return "Integer";
        if (type == Long.class || type == long.class) return "Long";
        if (type == Double.class || type == double.class) return "Double";
        if (type == Float.class || type == float.class) return "Float";
        if (type == Boolean.class || type == boolean.class) return "Boolean";
        return type.getSimpleName();
    }
}
