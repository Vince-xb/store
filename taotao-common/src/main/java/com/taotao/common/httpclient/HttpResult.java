package com.taotao.common.httpclient;

public class HttpResult {

    private Integer code;
    
    public HttpResult() {
        
    }
    

    public HttpResult(Integer code, String body) {
        super();
        this.code = code;
        this.body = body;
    }

    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
