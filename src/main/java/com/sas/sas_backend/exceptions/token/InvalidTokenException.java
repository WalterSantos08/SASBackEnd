package com.sas.sas_backend.exceptions.token;

import com.sas.sas_backend.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException(String detail) {
        super(HttpStatus.FORBIDDEN.toString(),"Token invalido", detail);
    }
}
