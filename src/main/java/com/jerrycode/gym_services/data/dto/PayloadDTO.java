package com.jerrycode.gym_services.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadDTO {
    @JsonProperty("before")
    private InvoicesDTO before;

    @JsonProperty("after")
    private InvoicesDTO after;

    @JsonProperty("source")
    private SourceDTO source;

    @JsonProperty("transaction")
    private Object transaction; // Can be null or a complex object

    @JsonProperty("op")
    private String operation;

    @JsonProperty("ts_ms")
    private Long tsMs;

    @JsonProperty("ts_us")
    private Long tsUs;

    @JsonProperty("ts_ns")
    private Long tsNs;

    public InvoicesDTO getBefore() { return before; }
    public void setBefore(InvoicesDTO before) { this.before = before; }

    public InvoicesDTO getAfter() { return after; }
    public void setAfter(InvoicesDTO after) { this.after = after; }

    public SourceDTO getSource() { return source; }
    public void setSource(SourceDTO source) { this.source = source; }

    public Object getTransaction() { return transaction; }
    public void setTransaction(Object transaction) { this.transaction = transaction; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public Long getTsMs() { return tsMs; }
    public void setTsMs(Long tsMs) { this.tsMs = tsMs; }

    public Long getTsUs() { return tsUs; }
    public void setTsUs(Long tsUs) { this.tsUs = tsUs; }

    public Long getTsNs() { return tsNs; }
    public void setTsNs(Long tsNs) { this.tsNs = tsNs; }
}
