package com.damon.web.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.damon.web.exception.CacheSessionException;

public interface SessionManager {
	/**
	 * 获取session
	 * 
	 * @param sessionId
	 * @param create
	 *            当为true时 如果httpSession为空,创建一个新的httpSession返回
	 *            当为false时,如果httpSession为空,返回null;
	 * @return
	 */
	CacheHttpSession getSession(HttpServletRequest request, HttpServletResponse response, RequestEventSubject subject, boolean create) throws CacheSessionException;

}
