package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.util.Map;

@Data
public class CreateSubmissionRequest {
    private Map<String, Object> answers;
}
