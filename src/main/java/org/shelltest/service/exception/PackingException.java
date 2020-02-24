package org.shelltest.service.exception;

import org.shelltest.service.utils.Constant;

public class PackingException extends Exception {
    private int resultCode;
    public PackingException() {
        super(Constant.ResultMsg.PACKING);
        this.resultCode = Constant.ResultCode.SHELL_ERROR;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
}
