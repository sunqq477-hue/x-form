package cn.xiaofuge.x.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormSubmissionPO {
    private Long id;
    private Long formId;
    private String answersJson;
    private LocalDateTime submittedAt;
}
