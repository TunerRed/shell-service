package org.shelltest.service.exception;

public class MyException extends Exception {
    private int resultCode;
    public MyException(int resultCode, String resultMsg) {
        super(resultMsg);
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return resultCode;
    }
}
