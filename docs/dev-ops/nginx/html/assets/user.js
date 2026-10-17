(function () {
  "use strict";

  const code = new URLSearchParams(window.location.search).get("code");
  let currentForm = null;
  const $ = (id) => document.getElementById(id);
  const escapeHtml = (value) => String(value == null ? "" : value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
  }[char]));

  function showError(message) {
    $("loading-state").hidden = true;
    $("form-view").hidden = true;
    $("error-state").hidden = false;
    $("error-message").textContent = message || "请检查链接是否正确，或联系表单创建者。";
  }

  function renderField(field) {
    const required = field.required ? "<span class=\"required\">*</span>" : "";
    const key = escapeHtml(field.key);
    const label = "<label class=\"field-label\" for=\"field-" + key + "\">" + escapeHtml(field.label) + required + "</label>";
    const placeholder = escapeHtml(field.placeholder || "");
    if (field.type === "textarea") {
      return "<div class=\"field\">" + label + "<textarea class=\"control\" id=\"field-" + key + "\" name=\"" + key + "\" placeholder=\"" + placeholder + "\"" + (field.required ? " required" : "") + "></textarea></div>";
    }
    if (field.type === "select") {
      const options = (field.options || []).map((option) => "<option value=\"" + escapeHtml(option) + "\">" + escapeHtml(option) + "</option>").join("");
      return "<div class=\"field\">" + label + "<select class=\"control\" id=\"field-" + key + "\" name=\"" + key + "\"" + (field.required ? " required" : "") + "><option value=\"\">请选择</option>" + options + "</select></div>";
    }
    if (field.type === "checkbox") {
      const options = (field.options || []).map((option) => (
        "<label class=\"choice\"><input type=\"checkbox\" name=\"" + key + "\" value=\"" + escapeHtml(option) + "\"><span>" + escapeHtml(option) + "</span></label>"
      )).join("");
      return "<fieldset class=\"field checkbox-field\" data-checkbox-key=\"" + key + "\" data-required=\"" + String(Boolean(field.required)) + "\" style=\"border:0;padding:0;margin-left:0;margin-right:0\">" +
        "<legend class=\"field-label\">" + escapeHtml(field.label) + required + "</legend><div class=\"choice-list\">" + options + "</div></fieldset>";
    }
    const inputType = ["email", "number", "date"].includes(field.type) ? field.type : "text";
    return "<div class=\"field\">" + label + "<input class=\"control\" id=\"field-" + key + "\" name=\"" + key + "\" type=\"" + inputType + "\" placeholder=\"" + placeholder + "\"" + (field.required ? " required" : "") + "></div>";
  }

  async function loadForm() {
    if (!code) {
      showError("分享链接缺少表单编号。请使用创建者提供的完整链接。");
      return;
    }
    try {
      currentForm = await window.XFormApi.public.getForm(code);
      if (!currentForm || !Array.isArray(currentForm.fields)) throw new Error("服务器返回的表单数据格式不正确。");
      document.title = currentForm.title + " · x-form";
      $("form-title").textContent = currentForm.title;
      $("form-description").textContent = currentForm.description || "";
      const form = $("submission-form");
      form.innerHTML = (currentForm.fields || []).map(renderField).join("") +
        "<div class=\"notice\" id=\"submit-notice\" role=\"status\"></div><button class=\"submit\" type=\"submit\">提交填写内容</button>";
      $("loading-state").hidden = true;
      $("form-view").hidden = false;
    } catch (error) {
      showError(error.message);
    }
  }

  $("submission-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const formElement = event.currentTarget;
    const notice = $("submit-notice");
    const button = formElement.querySelector(".submit");
    for (const field of currentForm.fields || []) {
      if (field.type === "checkbox" && field.required) {
        const fieldset = Array.from(formElement.querySelectorAll("fieldset[data-checkbox-key]"))
          .find((item) => item.dataset.checkboxKey === field.key);
        if (!fieldset || fieldset.querySelectorAll("input:checked").length === 0) {
          notice.textContent = "请选择：" + field.label;
          return;
        }
      }
    }

    const answers = {};
    for (const field of currentForm.fields || []) {
      if (field.type === "checkbox") {
        const fieldset = Array.from(formElement.querySelectorAll("fieldset[data-checkbox-key]"))
          .find((item) => item.dataset.checkboxKey === field.key);
        const values = fieldset
          ? Array.from(fieldset.querySelectorAll("input:checked")).map((input) => input.value)
          : [];
        if (values.length) answers[field.key] = values;
      } else {
        const input = formElement.elements.namedItem(field.key);
        if (input && input.value !== "") answers[field.key] = input.value;
      }
    }

    button.disabled = true;
    notice.textContent = "";
    try {
      await window.XFormApi.public.submit(code, answers);
      $("form-view").hidden = true;
      $("success-state").hidden = false;
    } catch (error) {
      notice.textContent = error.message;
    } finally {
      button.disabled = false;
    }
  });

  loadForm();
})();
