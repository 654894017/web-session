package com.damon.web.exception;

public class CacheSessionException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5594641262967019806L;

	public CacheSessionException() {
		super();
	}

	public CacheSessionException(String s){
		super(s);
	}
	
	public CacheSessionException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public CacheSessionException(Throwable cause) {
		super(cause);
	}
	
}
