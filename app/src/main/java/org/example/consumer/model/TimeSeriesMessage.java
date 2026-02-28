package org.example.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesMessage {


    @JsonProperty("additional_properties")
    private Map<String, String> additionalProperties;
}
