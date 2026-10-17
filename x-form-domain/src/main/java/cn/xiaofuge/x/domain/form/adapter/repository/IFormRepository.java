package cn.xiaofuge.x.domain.form.adapter.repository;

import cn.xiaofuge.x.domain.form.model.FormDefinition;
import cn.xiaofuge.x.domain.form.model.FormSubmission;

import java.util.List;

public interface IFormRepository {
    FormDefinition save(FormDefinition form);

    FormDefinition findById(Long id);

    FormDefinition findPublishedByShareCode(String shareCode);

    List<FormDefinition> findAll();

    FormSubmission saveSubmission(FormSubmission submission);

    List<FormSubmission> findSubmissions(Long formId);
}
