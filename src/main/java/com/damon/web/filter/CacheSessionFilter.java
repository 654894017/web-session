package com.damon.web.filter;

import java.io.IOException;
import java.util.EventListener;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.damon.web.session.HttpServletRequestSessionWrapper;
import com.damon.web.session.RequestEventSubject;
import com.damon.web.session.SessionManager;

/**
 * 
 * Session Filter
 * 
 * @author xianping_lu
 * 
 */
public class CacheSessionFilter implements Filter {

	public static final String[] IGNORE_SUFFIX = new String[] { ".png", ".jpg", ".jpeg", ".gif", ".css", ".js" };

	private List<EventListener> listeners;

	private SessionManager sessionManager;
	
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;

		HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (!shouldFilter(request)) {

			filterChain.doFilter(servletRequest, servletResponse);
			
		}else{
			
			RequestEventSubject subject = new RequestEventSubject();

			try {

				filterChain.doFilter(new HttpServletRequestSessionWrapper(request, response, sessionManager, subject, listeners), servletResponse);
			
			} finally {

				subject.completed(request, response);
			}
		}

		
	}

	private boolean shouldFilter(HttpServletRequest request) {

		String uri = request.getRequestURI().toLowerCase();

		for (String suffix : IGNORE_SUFFIX) {

			if (uri.endsWith(suffix)) {

				return false;
			}

		}
		return true;
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	public List<EventListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
	}

}
