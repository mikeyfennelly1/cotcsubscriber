package org.cotc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NatsJetstreamHealthResponse {

    private long memory;
    private long storage;

    @JsonProperty("reserved_memory")
    private long reservedMemory;

    @JsonProperty("reserved_storage")
    private long reservedStorage;

    private long accounts;

    @JsonProperty("ha_assets")
    private long haAssets;

    private ApiStats api;

    @JsonProperty("server_id")
    private String serverId;

    private String now;

    private Config config;

    private Map<String, Object> limits;

    private long streams;
    private long consumers;
    private long messages;
    private long bytes;
    private long total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiStats {
        private long level;
        private long total;
        private long errors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {

        @JsonProperty("max_memory")
        private long maxMemory;

        @JsonProperty("max_storage")
        private long maxStorage;

        @JsonProperty("store_dir")
        private String storeDir;

        @JsonProperty("sync_interval")
        private long syncInterval;

        private boolean strict;
    }
}
