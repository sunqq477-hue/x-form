package cn.xiaofuge.x.trigger.http.form;

import cn.xiaofuge.x.api.response.Response;

final class ApiResponses {
    private ApiResponses() {
    }

    static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setCode("0000");
        response.setInfo("success");
        response.setData(data);
        return response;
    }

    static Response<Object> failure(String code, String message) {
        Response<Object> response = new Response<>();
        response.setCode(code);
        response.setInfo(message);
        return response;
    }
}
