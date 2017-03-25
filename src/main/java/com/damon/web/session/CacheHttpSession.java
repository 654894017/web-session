package com.damon.web.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author xianping_lu
 * 
 */

@SuppressWarnings("deprecation")
public class CacheHttpSession implements HttpSession, Serializable {

	private final static Logger LOGGER = LoggerFactory.getLogger(CacheHttpSession.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -7331221145489414280L;
	private Map<String, Object> data = new ConcurrentHashMap<String, Object>();
	private long creationTime = 0L;
	private String sessionId;
	private int maxInactiveInterval;
	private long lastAccessedTime = 0L;
	private transient boolean expired = false;
	private transient boolean isNew = false;
	private transient boolean isDirty = false;
	private transient List<EventListener> listeners = new ArrayList<EventListener>();
	private transient ServletContext servletContext;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	public void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	@Override
	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public String getId() {
		return sessionId;
	}

	@Override
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	@Override
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return data.get(name);
	}

	@Override
	public Object getValue(String name) {
		return data.get(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Enumeration getAttributeNames() {
		return (new Enumerator(data.keySet(), true));
	}

	@Override
	public String[] getValueNames() {
		String[] names = new String[data.size()];
		return data.keySet().toArray(names);
	}

	@Override
	public void setAttribute(String name, Object value) {
		HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
		for (EventListener listener : listeners) {
			try {
				if (listener instanceof HttpSessionAttributeListener) {
					if (data.containsKey(name)) {
						((HttpSessionAttributeListener) listener).attributeReplaced(event);
					} else {
						((HttpSessionAttributeListener) listener).attributeAdded(event);
					}
				}
			} catch (Throwable throwable) {
				LOGGER.error("listener session update attribute fail ", throwable);
			}
		}
		data.put(name, value);
		isDirty = true;
	}

	@Override
	public void putValue(String name, Object value) {
		data.put(name, value);
		isDirty = true;
	}

	@Override
	public void removeAttribute(String name) {
		HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, data.get(name));
		data.remove(name);
		isDirty = true;
		for (EventListener listener : listeners) {
			try {
				if (listener instanceof HttpSessionAttributeListener) {
					((HttpSessionAttributeListener) listener).attributeRemoved(event);
				}
			} catch (Throwable throwable) {
				LOGGER.error("listener session attribute removed fail ", throwable);
			}
		}

	}

	@Override
	@Deprecated
	public void removeValue(String name) {
		HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, data.get(name));
		data.remove(name);
		isDirty = true;
		for (EventListener listener : listeners) {
			try {
				if (listener instanceof HttpSessionAttributeListener) {
					((HttpSessionAttributeListener) listener).attributeRemoved(event);
				}
			} catch (Throwable throwable) {
				LOGGER.error("listener session attribute removed fail ", throwable);
			}
		}
	}

	@Override
	public void invalidate() {
		expired = true;
		isDirty = true;
		HttpSessionEvent event = new HttpSessionEvent(this);
		for (EventListener listener : listeners) {
			try {
				if (listener instanceof HttpSessionListener) {
					((HttpSessionListener) listener).sessionDestroyed(event);
				}
			} catch (Throwable throwable) {
				LOGGER.error("listener session destroyed fail ", throwable);
			}
		}
	}

	@Override
	public boolean isNew() {
		return isNew;
	}

	public List<EventListener> getListeners() {
		return listeners;
	}

	public void setListeners(List<EventListener> listeners) {
		this.listeners = listeners;
		if (this.isNew) {
			HttpSessionEvent event = new HttpSessionEvent(this);
			for (EventListener listener : listeners) {
				try {
					if (listener instanceof HttpSessionListener) {
						((HttpSessionListener) listener).sessionCreated(event);
					}
				} catch (Throwable throwable) {
					LOGGER.error("listener session created fail ", throwable);
				}
			}
		}
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
