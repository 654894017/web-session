
package com.damon.web.session;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestEventObserver {
	
    public void completed(HttpServletRequest servletRequest, HttpServletResponse response);
}
