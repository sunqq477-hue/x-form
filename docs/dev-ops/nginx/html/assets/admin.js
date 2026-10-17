(function () {
  "use strict";

  const SESSION_KEY = "x-form-demo-operator";
  const PAGE_SIZE = 10;
  const state = {
    forms: [],
    page: 1,
    activeForm: null,
    activeSubmissions: []
  };
  const typeNames = {
    text: "单行文本", textarea: "多行文本", email: "邮箱", number: "数字",
    date: "日期", select: "单选", checkbox: "多选"
  };
  const $ = (id) => document.getElementById(id);
  const escapeHtml = (value) => String(value == null ? "" : value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;"
  }[char]));

  function setNotice(message) {
    $("notice").textContent = message || "";
  }

  function showToast(message) {
    const toast = $("toast");
    toast.textContent = message;
    toast.hidden = false;
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => { toast.hidden = true; }, 2600);
  }

  function formatDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString("zh-CN", { hour12: false });
  }

  function shareUrl(code) {
    return window.location.origin + "/user/?code=" + encodeURIComponent(code);
  }

  function currentOperator() {
    try { return sessionStorage.getItem(SESSION_KEY) || ""; } catch (_) { return ""; }
  }

  function setOperator(name) {
    try { sessionStorage.setItem(SESSION_KEY, name); } catch (_) { /* Private browsing may block storage. */ }
    renderSession();
  }

  function clearOperator() {
    try { sessionStorage.removeItem(SESSION_KEY); } catch (_) { /* Continue with the in-memory logout. */ }
    renderSession();
  }

  function renderSession() {
    const operator = currentOperator();
    $("login-screen").hidden = Boolean(operator);
    $("account-name").textContent = operator || "未登录";
    $("account-avatar").textContent = operator ? operator.slice(0, 1).toUpperCase() : "?";
    if (operator) loadForms();
  }

  function visibleForms() {
    const keyword = $("search").value.trim().toLocaleLowerCase();
    const status = $("status-filter").value;
    return state.forms.filter((form) => {
      const title = String(form.title || "").toLocaleLowerCase();
      const shareCode = String(form.shareCode || "").toLocaleLowerCase();
      return (!keyword || title.includes(keyword) || shareCode.includes(keyword)) &&
        (status === "all" || form.status === status);
    });
  }

  function renderForms() {
    const forms = visibleForms();
    const pageCount = Math.max(1, Math.ceil(forms.length / PAGE_SIZE));
    state.page = Math.min(Math.max(1, state.page), pageCount);
    const start = (state.page - 1) * PAGE_SIZE;
    const pageForms = forms.slice(start, start + PAGE_SIZE);
    $("form-count").textContent = "（" + forms.length + "）";
    $("empty-state").hidden = forms.length > 0;
    $("empty-state").textContent = state.forms.length === 0
      ? "还没有表单。点击“新建表单”开始创建。"
      : "没有符合当前筛选条件的表单。";
    $("form-rows").innerHTML = pageForms.map((form) => {
      const published = form.status === "PUBLISHED";
      const statusLabel = published ? "已发布" : (form.status === "DRAFT" ? "草稿" : "已停用");
      return "<tr>" +
        "<td><div class=\"form-name\">" + escapeHtml(form.title) + "</div><div class=\"form-sub\">分享码：" + escapeHtml(form.shareCode) + "</div></td>" +
        "<td><span class=\"badge" + (published ? "" : " inactive") + "\">" + statusLabel + "</span></td>" +
        "<td>" + Number(form.submissionCount || 0) + "</td>" +
        "<td>" + escapeHtml(formatDate(form.createdAt)) + "</td>" +
        "<td><div class=\"actions\"><button class=\"link-button\" data-action=\"responses\" data-id=\"" + escapeHtml(form.id) + "\">查看数据</button>" +
        (form.shareCode ? "<button class=\"link-button\" data-action=\"share\" data-code=\"" + escapeHtml(form.shareCode) + "\">复制链接</button>" : "") +
        "</div></td></tr>";
    }).join("");
    $("page-summary").textContent = forms.length
      ? "显示 " + (start + 1) + "–" + Math.min(start + PAGE_SIZE, forms.length) + " 条，共 " + forms.length + " 条"
      : "显示 0 条";
    $("page-number").textContent = state.page + " / " + pageCount;
    $("previous-page").disabled = state.page <= 1;
    $("next-page").disabled = state.page >= pageCount;

    $("stat-forms").textContent = String(state.forms.length);
    $("stat-published").textContent = String(state.forms.filter((form) => form.status === "PUBLISHED").length);
    $("stat-submissions").textContent = String(state.forms.reduce((sum, form) => sum + Number(form.submissionCount || 0), 0));
  }

  async function loadForms() {
    if (!currentOperator()) return;
    setNotice("");
    try {
      const result = await window.XFormApi.admin.listForms();
      state.forms = Array.isArray(result) ? result : [];
      renderForms();
    } catch (error) {
      setNotice("读取表单失败：" + error.message);
    }
  }

  function openModal(id) { $(id).hidden = false; }
  function closeModal(id) { $(id).hidden = true; }

  function setEditorNotice(message) {
    $("editor-notice").textContent = message || "";
  }

  function toggleOptions(row) {
    const type = row.querySelector(".field-type").value;
    row.querySelector(".field-options-wrap").hidden = type !== "select" && type !== "checkbox";
  }

  function addField(field) {
    const data = field || {
      key: "field_" + Date.now() + "_" + Math.floor(Math.random() * 1000),
      label: "", type: "text", required: false, placeholder: "", options: []
    };
    const row = document.createElement("div");
    row.className = "field-row";
    row.dataset.key = data.key;
    row.innerHTML =
      "<div class=\"field-controls\">" +
        "<label><span class=\"label\">字段名称</span><input class=\"control field-label\" maxlength=\"100\" value=\"" + escapeHtml(data.label) + "\" placeholder=\"例如：姓名\"></label>" +
        "<label><span class=\"label\">字段类型</span><select class=\"control field-type\">" +
          Object.keys(typeNames).map((type) => "<option value=\"" + type + "\"" + (data.type === type ? " selected" : "") + ">" + typeNames[type] + "</option>").join("") +
        "</select></label>" +
        "<label><span class=\"label\">提示文字</span><input class=\"control field-placeholder\" maxlength=\"200\" value=\"" + escapeHtml(data.placeholder || "") + "\" placeholder=\"可选\"></label>" +
        "<button class=\"remove-field\" type=\"button\">移除</button>" +
      "</div>" +
      "<div class=\"field-extra\">" +
        "<label class=\"field-options-wrap\"><span class=\"label\">选项（每行一个）</span><textarea class=\"control field-options\" rows=\"2\" placeholder=\"选项一&#10;选项二\">" + escapeHtml((data.options || []).join("\n")) + "</textarea></label>" +
        "<label class=\"required-toggle\"><input type=\"checkbox\" class=\"field-required\"" + (data.required ? " checked" : "") + "> 必填</label>" +
      "</div>";
    $("field-list").appendChild(row);
    row.querySelector(".field-type").addEventListener("change", () => toggleOptions(row));
    row.querySelector(".remove-field").addEventListener("click", () => row.remove());
    toggleOptions(row);
  }

  function collectFields() {
    return Array.from(document.querySelectorAll(".field-row")).map((row) => {
      const type = row.querySelector(".field-type").value;
      return {
        key: row.dataset.key,
        label: row.querySelector(".field-label").value.trim(),
        type: type,
        required: row.querySelector(".field-required").checked,
        placeholder: row.querySelector(".field-placeholder").value.trim(),
        options: type === "select" || type === "checkbox"
          ? row.querySelector(".field-options").value.split(/\r?\n/).map((value) => value.trim()).filter(Boolean)
          : []
      };
    });
  }

  function validateForm(title, fields) {
    if (!title) return "请填写表单名称。";
    if (title.length > 120) return "表单名称不能超过 120 个字符。";
    if (fields.length < 1 || fields.length > 30) return "表单需要 1 到 30 个字段。";
    for (let index = 0; index < fields.length; index += 1) {
      const field = fields[index];
      if (!field.label) return "请填写第 " + (index + 1) + " 个字段的名称。";
      if ((field.type === "select" || field.type === "checkbox") && field.options.length === 0) {
        return "请为字段“" + field.label + "”至少设置一个选项。";
      }
    }
    return "";
  }

  async function copyText(text, promptTitle) {
    try {
      await navigator.clipboard.writeText(text);
      showToast("填写链接已复制。");
    } catch (_) {
      window.prompt(promptTitle || "复制这条填写链接", text);
    }
  }

  async function createForm() {
    const button = $("save-form");
    const title = $("form-title").value.trim();
    const fields = collectFields();
    const validationError = validateForm(title, fields);
    if (validationError) {
      setEditorNotice(validationError);
      return;
    }

    button.disabled = true;
    button.textContent = "正在创建…";
    setEditorNotice("");
    try {
      const created = await window.XFormApi.admin.createForm({
        title: title,
        description: $("form-description").value.trim(),
        fields: fields
      });
      $("form-title").value = "";
      $("form-description").value = "";
      $("field-list").innerHTML = "";
      closeModal("editor-modal");
      await loadForms();
      showToast("表单已发布。");
      if (created && created.shareCode) {
        await copyText(shareUrl(created.shareCode), "复制这条填写链接");
      }
    } catch (error) {
      setEditorNotice("创建失败：" + error.message);
      setNotice("创建失败：" + error.message);
    } finally {
      button.disabled = false;
      button.textContent = "创建并发布";
    }
  }

  function renderSubmissions() {
    const form = state.activeForm;
    const keyword = $("submission-search").value.trim().toLocaleLowerCase();
    const rows = state.activeSubmissions.filter((submission) => {
      if (!keyword) return true;
      const values = form.fields.map((field) => {
        const value = submission.answers ? submission.answers[field.key] : "";
        return Array.isArray(value) ? value.join(" ") : String(value == null ? "" : value);
      }).join(" ");
      return (String(submission.id || "") + " " + values + " " + formatDate(submission.submittedAt)).toLocaleLowerCase().includes(keyword);
    });
    const columns = form.fields || [];
    $("submission-head").innerHTML = "<tr><th>记录编号</th><th>提交时间</th>" +
      columns.map((field) => "<th>" + escapeHtml(field.label) + "</th>").join("") + "</tr>";
    $("submission-empty").hidden = rows.length > 0;
    $("submission-empty").textContent = state.activeSubmissions.length === 0
      ? "这个表单还没有收到填写数据。"
      : "没有符合搜索条件的提交记录。";
    $("submission-rows").innerHTML = rows.map((submission) => {
      const cells = columns.map((field) => {
        const value = submission.answers ? submission.answers[field.key] : "";
        return "<td>" + escapeHtml(Array.isArray(value) ? value.join("、") : value) + "</td>";
      }).join("");
      return "<tr><td>" + escapeHtml(submission.id) + "</td><td>" + escapeHtml(formatDate(submission.submittedAt)) + "</td>" + cells + "</tr>";
    }).join("");
  }

  async function showSubmissions(formId) {
    const form = state.forms.find((item) => String(item.id) === String(formId));
    if (!form) return;
    state.activeForm = form;
    state.activeSubmissions = [];
    $("submissions-title").textContent = "填写数据 · " + form.title;
    $("submission-notice").textContent = "正在读取提交记录…";
    $("submission-search").value = "";
    $("submission-head").innerHTML = "";
    $("submission-rows").innerHTML = "";
    $("submission-empty").hidden = true;
    openModal("submissions-modal");
    try {
      const result = await window.XFormApi.admin.listSubmissions(formId);
      state.activeSubmissions = Array.isArray(result) ? result : [];
      $("submission-notice").textContent = "共 " + state.activeSubmissions.length + " 条提交记录。";
      renderSubmissions();
    } catch (error) {
      $("submission-notice").textContent = "读取填写数据失败：" + error.message;
    }
  }

  function exportSubmissions() {
    if (!state.activeForm) return;
    const columns = state.activeForm.fields || [];
    const header = ["记录编号", "提交时间"].concat(columns.map((field) => field.label));
    const quote = (value) => '"' + String(value == null ? "" : value).replace(/"/g, '""') + '"';
    const lines = [header.map(quote).join(",")].concat(state.activeSubmissions.map((submission) => {
      const values = [submission.id, formatDate(submission.submittedAt)].concat(columns.map((field) => {
        const value = submission.answers ? submission.answers[field.key] : "";
        return Array.isArray(value) ? value.join("、") : value;
      }));
      return values.map(quote).join(",");
    }));
    const blob = new Blob(["\uFEFF" + lines.join("\r\n")], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = (state.activeForm.title || "form") + "-填写数据.csv";
    anchor.click();
    URL.revokeObjectURL(url);
  }

  $("login-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const username = $("login-username").value.trim();
    const password = $("login-password").value;
    if (username !== "demo" || password !== "demo123") {
      $("login-error").textContent = "演示账号或密码不正确，请使用页面上显示的演示凭据。";
      return;
    }
    $("login-error").textContent = "";
    setOperator(username);
  });
  $("logout-button").addEventListener("click", clearOperator);
  $("open-create").addEventListener("click", () => {
    $("field-list").innerHTML = "";
    $("form-title").value = "";
    $("form-description").value = "";
    setEditorNotice("");
    addField();
    openModal("editor-modal");
    $("form-title").focus();
  });
  $("add-field").addEventListener("click", () => {
    if (document.querySelectorAll(".field-row").length >= 30) {
      setEditorNotice("每个表单最多添加 30 个字段。");
      return;
    }
    addField();
    setEditorNotice("");
  });
  $("save-form").addEventListener("click", createForm);
  $("search").addEventListener("input", () => { state.page = 1; renderForms(); });
  $("status-filter").addEventListener("change", () => { state.page = 1; renderForms(); });
  $("previous-page").addEventListener("click", () => { state.page -= 1; renderForms(); });
  $("next-page").addEventListener("click", () => { state.page += 1; renderForms(); });
  $("submission-search").addEventListener("input", renderSubmissions);
  $("export-submissions").addEventListener("click", exportSubmissions);
  document.querySelectorAll("[data-close]").forEach((button) => {
    button.addEventListener("click", () => closeModal(button.dataset.close));
  });
  $("form-rows").addEventListener("click", async (event) => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    if (button.dataset.action === "responses") {
      showSubmissions(button.dataset.id);
    } else if (button.dataset.action === "share") {
      await copyText(shareUrl(button.dataset.code));
    }
  });
  document.querySelector('[data-nav="submissions"]').addEventListener("click", () => {
    const first = visibleForms()[0];
    if (first) showSubmissions(first.id);
    else showToast("请先创建表单，再查看对应的填写数据。");
  });

  renderSession();
})();
