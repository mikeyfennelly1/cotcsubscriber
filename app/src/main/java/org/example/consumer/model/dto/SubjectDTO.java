package org.example.consumer.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubjectDTO {
    private Long id;
    private String name;
    private List<SubjectDTO> children;
    private List<SourceDTO> sources;
}
