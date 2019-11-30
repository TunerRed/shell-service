package org.shelltest.service.exception;

import org.shelltest.service.utils.Constant;
import org.shelltest.service.utils.ResponseBuilder;
import org.shelltest.service.utils.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class ErrorHandler {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity errorHandler (Exception e) {
        logger.error("意外的Exception："+e.getMessage());
        e.printStackTrace();
        return new ResponseBuilder().setCode(Constant.ResultCode.INTERNAL_ERROR).setMsg(e.getMessage()).getResponseEntity();
    }

    @ExceptionHandler(value = MyException.class)
    public ResponseEntity errorHandler (MyException e) {
        return new ResponseBuilder().setCode(e.getResultCode()).setMsg(e.getMessage()).getResponseEntity();
    }

    @ExceptionHandler(value = LoginException.class)
    public ResponseEntity errorHandler (LoginException e) {
        return new ResponseBuilder().setCode(e.getResultCode()).setMsg(e.getMessage()).getResponseEntity();
    }
}
