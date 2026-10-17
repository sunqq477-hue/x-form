package cn.xiaofuge.x.domain.form.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FormSubmission {
    private Long id;
    private Long formId;
    private Map<String, Object> answers;
    private LocalDateTime submittedAt;
}
