package cn.xiaofuge.x.domain.form.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FormDefinition {
    private Long id;
    private String shareCode;
    private String title;
    private String description;
    private String status;
    private List<FormField> fields;
    private long submissionCount;
    private LocalDateTime createdAt;
}
