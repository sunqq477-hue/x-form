(function (global) {
  "use strict";

  async function request(path, options) {
    let response;
    try {
      response = await fetch("/api" + path, Object.assign({
        headers: { "Content-Type": "application/json" }
      }, options || {}));
    } catch (error) {
      throw new Error("无法连接本机 API 代理（127.0.0.1:18080 → 127.0.0.1:8091）。请确认页面代理和 Spring Boot 后端都已启动。");
    }

    const responseText = await response.text();
    let body = null;
    if (responseText) {
      try {
        body = JSON.parse(responseText);
      } catch (error) {
        const hint = response.status === 502
          ? "后端暂不可用，请先启动 Spring Boot（8091 端口）。"
          : "服务返回了非 JSON 内容，请检查代理目标和后端日志。";
        throw new Error(hint + " HTTP " + response.status);
      }
    }

    const successfulCode = body && String(body.code) === "0000";
    if (!response.ok || !successfulCode) {
      const message = body && (body.info || body.message || body.error || body.detail);
      throw new Error(message || ("请求失败（HTTP " + response.status + "）"));
    }
    return body.data;
  }

  global.XFormApi = Object.freeze({
    admin: Object.freeze({
      listForms: function () {
        return request("/admin/forms");
      },
      createForm: function (form) {
        return request("/admin/forms", { method: "POST", body: JSON.stringify(form) });
      },
      listSubmissions: function (formId) {
        return request("/admin/forms/" + encodeURIComponent(formId) + "/submissions");
      }
    }),
    public: Object.freeze({
      getForm: function (shareCode) {
        return request("/public/forms/" + encodeURIComponent(shareCode));
      },
      submit: function (shareCode, answers) {
        return request("/public/forms/" + encodeURIComponent(shareCode) + "/submissions", {
          method: "POST",
          body: JSON.stringify({ answers: answers })
        });
      }
    })
  });
})(window);
