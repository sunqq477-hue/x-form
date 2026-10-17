package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class FormSubmissionDTO {
    private Long id;
    private Long formId;
    private Map<String, Object> answers;
    private LocalDateTime submittedAt;
}
