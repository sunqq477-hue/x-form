package cn.xiaofuge.x.domain.form.service;

import cn.xiaofuge.x.domain.form.model.FormDefinition;
import cn.xiaofuge.x.domain.form.model.FormField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FormDomainService {
    private static final Set<String> FIELD_TYPES = Set.of(
            "text", "textarea", "email", "number", "date", "select", "checkbox");
    private static final Pattern FIELD_KEY = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{0,39}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public FormDefinition create(String title, String description, List<FormField> fields) {
        String normalizedTitle = title == null ? "" : title.trim();
        if (normalizedTitle.isEmpty() || normalizedTitle.length() > 120) {
            throw new IllegalArgumentException("表单名称不能为空，且不能超过 120 个字符");
        }
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("表单说明不能超过 1000 个字符");
        }
        if (fields == null || fields.isEmpty() || fields.size() > 30) {
            throw new IllegalArgumentException("每个表单需要 1 到 30 个字段");
        }

        Set<String> keys = new HashSet<>();
        for (FormField field : fields) {
            if (field == null || field.getKey() == null || !FIELD_KEY.matcher(field.getKey()).matches()) {
                throw new IllegalArgumentException("字段标识格式不正确");
            }
            if (!keys.add(field.getKey())) {
                throw new IllegalArgumentException("字段标识不能重复");
            }
            if (field.getLabel() == null || field.getLabel().trim().isEmpty() || field.getLabel().length() > 100) {
                throw new IllegalArgumentException("字段名称不能为空，且不能超过 100 个字符");
            }
            if (field.getType() == null || !FIELD_TYPES.contains(field.getType())) {
                throw new IllegalArgumentException("字段类型不受支持");
            }
            if (("select".equals(field.getType()) || "checkbox".equals(field.getType()))
                    && (field.getOptions() == null || field.getOptions().isEmpty())) {
                throw new IllegalArgumentException("单选和多选字段至少需要一个选项");
            }
            if (field.getOptions() != null) {
                List<String> options = field.getOptions().stream()
                        .filter(option -> option != null && !option.trim().isEmpty())
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
                field.setOptions(options);
            }
            if (("select".equals(field.getType()) || "checkbox".equals(field.getType()))
                    && (field.getOptions() == null || field.getOptions().isEmpty())) {
                throw new IllegalArgumentException("单选和多选字段至少需要一个有效选项");
            }
        }

        FormDefinition form = new FormDefinition();
        form.setShareCode(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        form.setTitle(normalizedTitle);
        form.setDescription(description == null ? "" : description.trim());
        form.setStatus("PUBLISHED");
        form.setFields(fields);
        return form;
    }

    public void validateAnswers(FormDefinition form, Map<String, Object> answers) {
        Map<String, Object> safeAnswers = answers == null ? Map.of() : answers;
        Set<String> allowedKeys = new HashSet<>();

        for (FormField field : form.getFields()) {
            allowedKeys.add(field.getKey());
            Object value = safeAnswers.get(field.getKey());
            if (field.isRequired() && !hasValue(value)) {
                throw new IllegalArgumentException("请填写：" + field.getLabel());
            }
            if (!hasValue(value)) {
                continue;
            }

            String stringValue = value instanceof String ? ((String) value).trim() : String.valueOf(value);
            switch (field.getType()) {
                case "text":
                case "textarea":
                    if (!(value instanceof String) || stringValue.length() > 4000) {
                        throw new IllegalArgumentException(field.getLabel() + "内容格式不正确或过长");
                    }
                    break;
                case "email":
                    if (!(value instanceof String) || !EMAIL.matcher(stringValue).matches()) {
                        throw new IllegalArgumentException(field.getLabel() + "不是有效邮箱地址");
                    }
                    break;
                case "number":
                    try {
                        new BigDecimal(stringValue);
                    } catch (NumberFormatException exception) {
                        throw new IllegalArgumentException(field.getLabel() + "必须是数字");
                    }
                    break;
                case "date":
                    try {
                        LocalDate.parse(stringValue);
                    } catch (RuntimeException exception) {
                        throw new IllegalArgumentException(field.getLabel() + "日期格式不正确");
                    }
                    break;
                case "select":
                    if (!(value instanceof String) || field.getOptions() == null
                            || !field.getOptions().contains(stringValue)) {
                        throw new IllegalArgumentException(field.getLabel() + "选项无效");
                    }
                    break;
                case "checkbox":
                    if (!(value instanceof List) || field.getOptions() == null
                            || !field.getOptions().containsAll((List<?>) value)) {
                        throw new IllegalArgumentException(field.getLabel() + "选项无效");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("字段类型不受支持");
            }
        }

        for (String key : safeAnswers.keySet()) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException("提交数据中包含未定义字段");
            }
        }
    }

    private boolean hasValue(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return !((String) value).trim().isEmpty();
        }
        if (value instanceof List) {
            return !((List<?>) value).isEmpty();
        }
        return true;
    }
}
