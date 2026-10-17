package cn.xiaofuge.x.infrastructure.dao.po;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormDefinitionPO {
    private Long id;
    private String shareCode;
    private String title;
    private String description;
    private String status;
    private String fieldsJson;
    private long submissionCount;
    private LocalDateTime createdAt;
}
