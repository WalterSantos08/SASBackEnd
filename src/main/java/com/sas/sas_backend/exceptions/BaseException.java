package com.sas.sas_backend.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BaseException extends RuntimeException {

    private final String httpStatusCode;
    private final String title;
    private final String detail;

}

