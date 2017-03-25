package com.damon.web.session;

import java.util.EventListener;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpServletRequestSessionWrapper extends HttpServletRequestWrapper {

	private SessionManager sessionManager;

	private CacheHttpSession httpSession;

	private HttpServletResponse response;

	private RequestEventSubject subject;

	private List<EventListener> listeners;

	private ServletContext servletContext;

	public HttpServletRequestSessionWrapper(HttpServletRequest request, HttpServletResponse response, SessionManager sessionManager, 
			
			RequestEventSubject subject, List<EventListener> listeners) {

		super(request);

		this.sessionManager = sessionManager;

		this.response = response;

		this.subject = subject;

		this.listeners = listeners;

		this.servletContext = request.getServletContext();

	}

	@Override
	public HttpSession getSession(boolean create) {

		if (httpSession != null) {

			return httpSession;
		
		}else{
			
			httpSession = sessionManager.getSession(this, response, subject, create);

			if (httpSession != null) {

				httpSession.setListeners(listeners);

				httpSession.setServletContext(servletContext);

			}
			
			return httpSession;
			
		}
		
	}

	@Override
	public HttpSession getSession() {

		return getSession(true);
	}

}
