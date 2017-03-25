package com.damon.web.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestEventSubject {
	
	private RequestEventObserver eventObserver;

	public void attach(RequestEventObserver eventObserver) {
		
		this.eventObserver = eventObserver;
	}

	public void detach() {
		
		eventObserver = null;
	}

	public void completed(HttpServletRequest servletRequest, HttpServletResponse response) {

		if (eventObserver != null){
			
			eventObserver.completed(servletRequest, response);
		}
	}
}
