package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.util.List;

@Data
public class CreateFormRequest {
    private String title;
    private String description;
    private List<FormFieldDTO> fields;
}
