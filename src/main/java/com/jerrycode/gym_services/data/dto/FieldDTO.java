package com.jerrycode.gym_services.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


public class FieldDTO {
    private String type;
    private String name;
    private boolean optional;
    private String field;
    private List<FieldDTO> fields; // Added to support nested fields
    private Integer version;
    @JsonProperty("default")
    private Object defaultValue;
    private Map<String, Object> parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<FieldDTO> getFields() {
        return fields;
    }

    public void setFields(List<FieldDTO> fields) {
        this.fields = fields;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
