package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionReceiptDTO {
    private Long id;
    private LocalDateTime submittedAt;
}
