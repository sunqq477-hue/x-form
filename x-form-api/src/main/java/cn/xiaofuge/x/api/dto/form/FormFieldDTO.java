package cn.xiaofuge.x.api.dto.form;

import lombok.Data;

import java.util.List;

@Data
public class FormFieldDTO {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private String placeholder;
    private List<String> options;
}
