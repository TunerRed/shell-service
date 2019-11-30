package org.shelltest.service.utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
    private ResponseEntity responseEntity;
    Map<String, Object> data;

    public ResponseBuilder() {
        responseEntity = new ResponseEntity();
    }

    public ResponseBuilder setCode(int resultCode) {
        responseEntity.setResultCode(resultCode);
        return this;
    }
    public ResponseBuilder setMsg(String resultMsg) {
        responseEntity.setResultMsg(resultMsg);
        return this;
    }
    public ResponseBuilder setData(Object object) {
        responseEntity.setResultData(object);
        if (data != null && !data.isEmpty())
            data.clear();
        return this;
    }
    public ResponseBuilder putItem(String name, Object value) {
        if (data == null)
            data = new HashMap<>();
        data.put(name, value);
        return this;
    }
    public ResponseEntity getResponseEntity() {
        if (data != null && data.size() != 0)
            responseEntity.setResultData(data);
        return responseEntity;
    }
}
