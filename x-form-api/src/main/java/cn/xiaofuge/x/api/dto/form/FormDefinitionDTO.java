package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FormDefinitionDTO {
    private Long id;
    private String shareCode;
    private String title;
    private String description;
    private String status;
    private List<FormFieldDTO> fields;
    private long submissionCount;
    private LocalDateTime createdAt;
}
