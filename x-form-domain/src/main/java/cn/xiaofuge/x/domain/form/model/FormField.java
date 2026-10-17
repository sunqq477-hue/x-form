package cn.xiaofuge.x.domain.form.model;

import lombok.Data;

import java.util.List;

@Data
public class FormField {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private String placeholder;
    private List<String> options;
}
