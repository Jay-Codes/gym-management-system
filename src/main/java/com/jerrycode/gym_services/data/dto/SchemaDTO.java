package com.jerrycode.gym_services.data.dto;

import java.util.List;

public class SchemaDTO {
    private String type;
    private List<FieldDTO> fields;
    private boolean optional;
    private String name;
    private Integer version;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void setFields(List<FieldDTO> fields) {
        this.fields = fields;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }


}
