package com.jerrycode.gym_services.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DebeziumEventDTO {

    @JsonProperty("schema")
    private SchemaDTO schema;

    @JsonProperty("payload")
    private PayloadDTO payload;

    public SchemaDTO getSchema() {
        return schema;
    }

    public void setSchema(SchemaDTO schema) {
        this.schema = schema;
    }

    public PayloadDTO getPayload() {
        return payload;
    }

    public void setPayload(PayloadDTO payload) {
        this.payload = payload;
    }
}
