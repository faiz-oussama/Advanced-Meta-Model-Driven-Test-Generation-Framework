package com.univade.TU.generator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipMetaModel {
    
    private String name;
    private String type;
    private String targetEntity;
    private String mappedBy;
    private String joinColumn;
    private String joinTable;
    private String cascadeType;
    private String fetchType;
    private boolean orphanRemoval;
    private boolean optional;

    public String getNameCapitalized() {
        if (name == null || name.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public String getGetterName() {
        return "get" + getNameCapitalized();
    }

    public String getSetterName() {
        return "set" + getNameCapitalized();
    }

    public boolean isOneToOne() {
        return "OneToOne".equals(type);
    }

    public boolean isOneToMany() {
        return "OneToMany".equals(type);
    }

    public boolean isManyToOne() {
        return "ManyToOne".equals(type);
    }

    public boolean isManyToMany() {
        return "ManyToMany".equals(type);
    }

    public boolean isCollection() {
        return isOneToMany() || isManyToMany();
    }

    public boolean isOwner() {
        return mappedBy == null || mappedBy.isEmpty();
    }

    public String getJavaType() {
        if (isCollection()) {
            return "java.util.List<" + targetEntity + ">";
        }
        return targetEntity;
    }

    public String getCollectionInitializer() {
        if (isCollection()) {
            return "new java.util.ArrayList<>()";
        }
        return null;
    }
}
