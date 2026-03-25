package org.cotc.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamStatusResponse {

    private String stream;
    private List<ConsumerStatus> consumers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerStatus {

        private String consumer;

        private long pending;

        @JsonProperty("ack_pending")
        private long ackPending;

        private long redelivered;
    }
}
