package com.musala.gateway.exceptions;

/**
 * @author lutfun
 * @since 6/23/21
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
