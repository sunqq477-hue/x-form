package cn.xiaofuge.x.infrastructure.adapter.repository;

import cn.xiaofuge.x.domain.form.adapter.repository.IFormRepository;
import cn.xiaofuge.x.domain.form.model.FormDefinition;
import cn.xiaofuge.x.domain.form.model.FormField;
import cn.xiaofuge.x.domain.form.model.FormSubmission;
import cn.xiaofuge.x.infrastructure.dao.IFormDefinitionDao;
import cn.xiaofuge.x.infrastructure.dao.IFormSubmissionDao;
import cn.xiaofuge.x.infrastructure.dao.po.FormDefinitionPO;
import cn.xiaofuge.x.infrastructure.dao.po.FormSubmissionPO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class FormRepository implements IFormRepository {
    private final IFormDefinitionDao formDefinitionDao;
    private final IFormSubmissionDao formSubmissionDao;

    public FormRepository(IFormDefinitionDao formDefinitionDao, IFormSubmissionDao formSubmissionDao) {
        this.formDefinitionDao = formDefinitionDao;
        this.formSubmissionDao = formSubmissionDao;
    }

    @Override
    public FormDefinition save(FormDefinition form) {
        FormDefinitionPO po = new FormDefinitionPO();
        po.setShareCode(form.getShareCode());
        po.setTitle(form.getTitle());
        po.setDescription(form.getDescription());
        po.setStatus(form.getStatus());
        po.setFieldsJson(JSON.toJSONString(form.getFields()));
        formDefinitionDao.insert(po);
        form.setId(po.getId());
        form.setCreatedAt(LocalDateTime.now());
        form.setSubmissionCount(0);
        return form;
    }

    @Override
    public FormDefinition findById(Long id) {
        return toDomain(formDefinitionDao.findById(id));
    }

    @Override
    public FormDefinition findPublishedByShareCode(String shareCode) {
        return toDomain(formDefinitionDao.findPublishedByShareCode(shareCode));
    }

    @Override
    public List<FormDefinition> findAll() {
        return formDefinitionDao.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public FormSubmission saveSubmission(FormSubmission submission) {
        FormSubmissionPO po = new FormSubmissionPO();
        po.setFormId(submission.getFormId());
        po.setAnswersJson(JSON.toJSONString(submission.getAnswers()));
        formSubmissionDao.insert(po);
        submission.setId(po.getId());
        submission.setSubmittedAt(LocalDateTime.now());
        return submission;
    }

    @Override
    public List<FormSubmission> findSubmissions(Long formId) {
        List<FormSubmission> submissions = new ArrayList<>();
        for (FormSubmissionPO po : formSubmissionDao.findByFormId(formId)) {
            FormSubmission submission = new FormSubmission();
            submission.setId(po.getId());
            submission.setFormId(po.getFormId());
            submission.setAnswers(JSON.parseObject(
                    po.getAnswersJson(), new TypeReference<Map<String, Object>>() {}));
            submission.setSubmittedAt(po.getSubmittedAt());
            submissions.add(submission);
        }
        return submissions;
    }

    private FormDefinition toDomain(FormDefinitionPO po) {
        if (po == null) {
            return null;
        }
        FormDefinition form = new FormDefinition();
        form.setId(po.getId());
        form.setShareCode(po.getShareCode());
        form.setTitle(po.getTitle());
        form.setDescription(po.getDescription());
        form.setStatus(po.getStatus());
        form.setFields(JSON.parseArray(po.getFieldsJson(), FormField.class));
        form.setSubmissionCount(po.getSubmissionCount());
        form.setCreatedAt(po.getCreatedAt());
        return form;
    }
}
