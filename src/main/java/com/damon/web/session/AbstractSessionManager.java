package com.damon.web.session;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damon.web.exception.CacheSessionException;

/**
 * 
 * @author xianping_lu
 * 
 */
public abstract class AbstractSessionManager implements SessionManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractSessionManager.class);

	private String cookieName;

	private String cookiePath;

	/**
	 * 如果session没有变化，则5分钟更新一次memcached 针对部分缓存框架不支持 touch操作
	 */
	protected int expirationUpdateInterval;

	/**
	 * session的最大存活时间 1800秒
	 */
	protected int maxInactiveInterval;
	/**
	 * https协议传输
	 */
	private boolean secure = false;
	/**
	 * 拒绝js获取cookie j2ee6 jar包
	 */
	private boolean httpOnly = true;
	/**
	 * 浏览器关闭删除cookie
	 */
	private int maxAge = -1;

	protected String sessionIdPrefix;

	public String getSessionIdPrefix() {
		return sessionIdPrefix;
	}

	public void setSessionIdPrefix(String sessionIdPrefix) {
		this.sessionIdPrefix = sessionIdPrefix;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public String getCookieName() {
		return cookieName;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public int getExpirationUpdateInterval() {
		return expirationUpdateInterval;
	}

	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	/**
	 * 获取前端传过来的cookie的值
	 * 
	 * @param request
	 * @return
	 */
	protected String getRequestedSessionId(HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();

		if (cookies == null || cookies.length == 0) {

			return null;
		}

		for (Cookie cookie : cookies) {

			if (cookieName.equals(cookie.getName())) {

				return cookie.getValue();
			}
		}

		return null;
	}

	/**
	 * 保存sessionId到cookie
	 * 
	 * @param session
	 * @param request
	 * @param response
	 */
	private void saveCookie(CacheHttpSession session, HttpServletRequest request, HttpServletResponse response) {

		if (session.isNew() || session.isExpired()) {

			String sessionId = session.getId();

			Cookie cookie = new Cookie(cookieName, sessionId);
			// cookie生命周期 -1浏览器关闭 删除cookie
			cookie.setMaxAge(maxAge);

			String host = request.getHeader("host");

			if (host.indexOf(":") > -1) {

				host = host.split(":")[0];
			}
			// 是否https协议下传输cookie
			cookie.setSecure(secure);
			// 拒绝js获取cookie j2ee6 jar包
			cookie.setHttpOnly(httpOnly);

			cookie.setDomain(host);

			cookie.setPath(cookiePath);

			cookie.setValue(session.getId());

			response.addCookie(cookie);

			LOGGER.debug("############  CacheHttpSession saveCookie [ID=" + sessionId + "]");

		} 

	}

	protected CacheHttpSession createEmptySession(HttpServletRequest request, HttpServletResponse response) {

		CacheHttpSession session = new CacheHttpSession();

		String sessionId = UUID.randomUUID().toString();

		session.setSessionId(sessionId);

		session.setCreationTime(System.currentTimeMillis());

		session.setMaxInactiveInterval(this.maxInactiveInterval);

		session.setLastAccessedTime(System.currentTimeMillis());

		session.setNew(true);

		this.saveCookie(session, request, response);

		return session;
	}

	@Override
	public CacheHttpSession getSession(HttpServletRequest request, HttpServletResponse response, RequestEventSubject subject, boolean create) {

		String sessionId = this.getRequestedSessionId(request);

		if ((sessionId == null || sessionId.length() == 0) && !create) {

			return null;

		} else {

			CacheHttpSession session = this.getSession(sessionId);

			if (session == null && create) {

				session = this.createEmptySession(request, response);
			}

			if (session != null) {

				this.attachEvent(session, request, response, subject);
			}

			return session;
		}

	}

	protected void attachEvent(final CacheHttpSession session, final HttpServletRequest request, final HttpServletResponse response, RequestEventSubject subject) {

		subject.attach(new RequestEventObserver() {

			public void completed(HttpServletRequest servletRequest, HttpServletResponse response) {

				if (session.isExpired()) {

					deleteSession(session.getId());

				} else {

					int updateInterval = (int) ((System.currentTimeMillis() - session.getLastAccessedTime()) / 1000);

					// when session is new  or is dirty or greater than  update interval , need update session info to storage
					
					if (session.isNew() || session.isDirty() || updateInterval > expirationUpdateInterval) {

						session.setLastAccessedTime(System.currentTimeMillis());

						LOGGER.debug("######### CacheHttpSession Request completed [ID={},lastAccessedTime={},updateInterval={}]", session.getId(), session.getLastAccessedTime(), updateInterval);
						
						if (session.isNew()) {

							saveSession(session);

						} else {

							updateSession(session);
						}
					}
				}
			}
		});
	}

	public void setCookieName(String cookieName) {
		this.cookieName = cookieName;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	public void setExpirationUpdateInterval(int expirationUpdateInterval) {
		this.expirationUpdateInterval = expirationUpdateInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	protected abstract CacheHttpSession getSession(String sessionId) throws CacheSessionException;

	protected abstract void deleteSession(String sessionId) throws CacheSessionException;

	protected abstract void updateSession(CacheHttpSession httpSession) throws CacheSessionException;

	protected abstract void saveSession(CacheHttpSession httpSession) throws CacheSessionException;

}
