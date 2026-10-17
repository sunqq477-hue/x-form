package cn.xiaofuge.x.cases.form;

import cn.xiaofuge.x.api.dto.form.CreateFormRequest;
import cn.xiaofuge.x.api.dto.form.FormDefinitionDTO;
import cn.xiaofuge.x.api.dto.form.FormFieldDTO;
import cn.xiaofuge.x.api.dto.form.FormSubmissionDTO;
import cn.xiaofuge.x.api.dto.form.SubmissionReceiptDTO;
import cn.xiaofuge.x.domain.form.adapter.port.IFormDefinitionCache;
import cn.xiaofuge.x.domain.form.adapter.repository.IFormRepository;
import cn.xiaofuge.x.domain.form.model.FormDefinition;
import cn.xiaofuge.x.domain.form.model.FormField;
import cn.xiaofuge.x.domain.form.model.FormSubmission;
import cn.xiaofuge.x.domain.form.service.FormDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class FormCaseImpl implements IFormCase {
    private final IFormRepository formRepository;
    private final IFormDefinitionCache formDefinitionCache;
    private final FormDomainService formDomainService = new FormDomainService();

    public FormCaseImpl(IFormRepository formRepository, IFormDefinitionCache formDefinitionCache) {
        this.formRepository = formRepository;
        this.formDefinitionCache = formDefinitionCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FormDefinitionDTO createForm(CreateFormRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("表单请求不能为空");
        }
        FormDefinition form = formDomainService.create(
                request.getTitle(), request.getDescription(), toDomainFields(request.getFields()));
        FormDefinition saved = formRepository.save(form);
        formDefinitionCache.evict(saved.getShareCode());
        return toDTO(saved);
    }

    @Override
    public List<FormDefinitionDTO> listForms() {
        return formRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public FormDefinitionDTO getPublicForm(String shareCode) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            throw new IllegalArgumentException("分享码不能为空");
        }
        FormDefinition form = formDefinitionCache.get(shareCode);
        if (form == null) {
            form = formRepository.findPublishedByShareCode(shareCode);
            if (form != null) {
                formDefinitionCache.put(form);
            }
        }
        if (form == null) {
            throw new NoSuchElementException("表单不存在或已下线");
        }
        return toDTO(form);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmissionReceiptDTO submit(String shareCode, Map<String, Object> answers) {
        FormDefinition form = formRepository.findPublishedByShareCode(shareCode);
        if (form == null) {
            throw new NoSuchElementException("表单不存在或已下线");
        }
        formDomainService.validateAnswers(form, answers);

        FormSubmission submission = new FormSubmission();
        submission.setFormId(form.getId());
        submission.setAnswers(answers);
        FormSubmission saved = formRepository.saveSubmission(submission);

        SubmissionReceiptDTO receipt = new SubmissionReceiptDTO();
        receipt.setId(saved.getId());
        receipt.setSubmittedAt(saved.getSubmittedAt());
        return receipt;
    }

    @Override
    public List<FormSubmissionDTO> listSubmissions(Long formId) {
        if (formRepository.findById(formId) == null) {
            throw new NoSuchElementException("表单不存在");
        }
        return formRepository.findSubmissions(formId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private List<FormField> toDomainFields(List<FormFieldDTO> fields) {
        if (fields == null) {
            return null;
        }
        return fields.stream().map(field -> {
            if (field == null) {
                return null;
            }
            FormField domainField = new FormField();
            domainField.setKey(field.getKey());
            domainField.setLabel(field.getLabel());
            domainField.setType(field.getType());
            domainField.setRequired(field.isRequired());
            domainField.setPlaceholder(field.getPlaceholder());
            domainField.setOptions(field.getOptions());
            return domainField;
        }).collect(Collectors.toList());
    }

    private FormDefinitionDTO toDTO(FormDefinition form) {
        FormDefinitionDTO dto = new FormDefinitionDTO();
        dto.setId(form.getId());
        dto.setShareCode(form.getShareCode());
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setStatus(form.getStatus());
        dto.setFields(form.getFields().stream().map(field -> {
            FormFieldDTO fieldDTO = new FormFieldDTO();
            fieldDTO.setKey(field.getKey());
            fieldDTO.setLabel(field.getLabel());
            fieldDTO.setType(field.getType());
            fieldDTO.setRequired(field.isRequired());
            fieldDTO.setPlaceholder(field.getPlaceholder());
            fieldDTO.setOptions(field.getOptions());
            return fieldDTO;
        }).collect(Collectors.toList()));
        dto.setSubmissionCount(form.getSubmissionCount());
        dto.setCreatedAt(form.getCreatedAt());
        return dto;
    }

    private FormSubmissionDTO toDTO(FormSubmission submission) {
        FormSubmissionDTO dto = new FormSubmissionDTO();
        dto.setId(submission.getId());
        dto.setFormId(submission.getFormId());
        dto.setAnswers(submission.getAnswers());
        dto.setSubmittedAt(submission.getSubmittedAt());
        return dto;
    }
}
