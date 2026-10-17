package cn.xiaofuge.x.trigger.http.form;

import cn.xiaofuge.x.api.dto.form.CreateFormRequest;
import cn.xiaofuge.x.api.dto.form.CreateSubmissionRequest;
import cn.xiaofuge.x.api.response.Response;
import cn.xiaofuge.x.cases.form.IFormCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class FormController {
    private final IFormCase formCase;

    public FormController(IFormCase formCase) {
        this.formCase = formCase;
    }

    @GetMapping("/admin/forms")
    public Response<?> listForms() {
        return ApiResponses.success(formCase.listForms());
    }

    @PostMapping("/admin/forms")
    public Response<?> createForm(@RequestBody CreateFormRequest request) {
        return ApiResponses.success(formCase.createForm(request));
    }

    @GetMapping("/admin/forms/{formId}/submissions")
    public Response<?> listSubmissions(@PathVariable Long formId) {
        return ApiResponses.success(formCase.listSubmissions(formId));
    }

    @GetMapping("/public/forms/{shareCode}")
    public Response<?> getPublicForm(@PathVariable String shareCode) {
        return ApiResponses.success(formCase.getPublicForm(shareCode));
    }

    @PostMapping("/public/forms/{shareCode}/submissions")
    public Response<?> submit(@PathVariable String shareCode, @RequestBody CreateSubmissionRequest request) {
        return ApiResponses.success(formCase.submit(
                shareCode, request == null ? Collections.emptyMap() : request.getAnswers()));
    }
}
