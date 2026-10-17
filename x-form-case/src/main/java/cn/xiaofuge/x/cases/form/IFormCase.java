package cn.xiaofuge.x.cases.form;

import cn.xiaofuge.x.api.dto.form.CreateFormRequest;
import cn.xiaofuge.x.api.dto.form.FormDefinitionDTO;
import cn.xiaofuge.x.api.dto.form.FormSubmissionDTO;
import cn.xiaofuge.x.api.dto.form.SubmissionReceiptDTO;

import java.util.List;
import java.util.Map;

public interface IFormCase {
    FormDefinitionDTO createForm(CreateFormRequest request);

    List<FormDefinitionDTO> listForms();

    FormDefinitionDTO getPublicForm(String shareCode);

    SubmissionReceiptDTO submit(String shareCode, Map<String, Object> answers);

    List<FormSubmissionDTO> listSubmissions(Long formId);
}
