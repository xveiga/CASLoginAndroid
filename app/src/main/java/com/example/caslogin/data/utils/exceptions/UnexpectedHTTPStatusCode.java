package com.example.caslogin.data.utils.exceptions;

public class UnexpectedHTTPStatusCode extends Exception {

    public UnexpectedHTTPStatusCode() {
        super();
    }

    public UnexpectedHTTPStatusCode(String message) {
        super(message);
    }
}
