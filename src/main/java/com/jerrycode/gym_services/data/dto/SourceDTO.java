package com.jerrycode.gym_services.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SourceDTO {
    @JsonProperty("version")
    private String version;

    @JsonProperty("connector")
    private String connector;

    @JsonProperty("name")
    private String name;

    @JsonProperty("ts_ms")
    private Long tsMs;

    @JsonProperty("ts_us")
    private Long tsUs;

    @JsonProperty("ts_ns")
    private Long tsNs;

    @JsonProperty("snapshot")
    private String snapshot;

    @JsonProperty("db")
    private String db;

    @JsonProperty("table")
    private String table;

    @JsonProperty("server_id")
    private Long serverId;

    @JsonProperty("gtid")
    private String gtid;

    @JsonProperty("file")
    private String file;

    @JsonProperty("pos")
    private Long pos;

    @JsonProperty("row")
    private Integer row;

    @JsonProperty("thread")
    private Long thread;

    @JsonProperty("query")
    private String query;

    @JsonProperty("sequence")
    private String sequence;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getConnector() { return connector; }
    public void setConnector(String connector) { this.connector = connector; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getTsMs() { return tsMs; }
    public void setTsMs(Long tsMs) { this.tsMs = tsMs; }

    public Long getTsUs() { return tsUs; }
    public void setTsUs(Long tsUs) { this.tsUs = tsUs; }

    public Long getTsNs() { return tsNs; }
    public void setTsNs(Long tsNs) { this.tsNs = tsNs; }

    public String getSnapshot() { return snapshot; }
    public void setSnapshot(String snapshot) { this.snapshot = snapshot; }

    public String getDb() { return db; }
    public void setDb(String db) { this.db = db; }

    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }

    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }

    public String getGtid() { return gtid; }
    public void setGtid(String gtid) { this.gtid = gtid; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public Long getPos() { return pos; }
    public void setPos(Long pos) { this.pos = pos; }

    public Integer getRow() { return row; }
    public void setRow(Integer row) { this.row = row; }

    public Long getThread() { return thread; }
    public void setThread(Long thread) { this.thread = thread; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
}
