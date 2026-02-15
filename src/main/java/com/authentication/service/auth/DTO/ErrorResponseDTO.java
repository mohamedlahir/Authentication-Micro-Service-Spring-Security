package com.authentication.service.auth.DTO;


public class ErrorResponseDTO {

    private String errorMessage;
    private int errorCode;

    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
                "errorMessage='" + errorMessage + '\'' +
                ", errorCode=" + errorCode +
                '}';
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
