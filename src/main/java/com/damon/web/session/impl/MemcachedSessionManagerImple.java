package com.damon.web.session.impl;

import net.rubyeye.xmemcached.CASOperation;
import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damon.web.exception.CacheSessionException;
import com.damon.web.session.AbstractSessionManager;
import com.damon.web.session.CacheHttpSession;

/**
 * 
 * @author xianping_lu
 * 
 */
public class MemcachedSessionManagerImple extends AbstractSessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedSessionManagerImple.class);

	private MemcachedClient memcachedSessionClient;

	public MemcachedClient getMemcachedSessionClient() {
		return memcachedSessionClient;
	}

	public void setMemcachedSessionClient(MemcachedClient memcachedSessionClient) {
		this.memcachedSessionClient = memcachedSessionClient;
	}

	@Override
	public CacheHttpSession getSession(String sessionId) throws CacheSessionException {

		CacheHttpSession session = null;

		try {
			session = memcachedSessionClient.get(super.sessionIdPrefix + sessionId);

			if (session != null) {

				session.setNew(false);

				session.setDirty(false);
			}
		} catch (Exception e) {

			throw new CacheSessionException("#################### CacheHttpSession getSession sessionId [ID=" + sessionId + "] error", e);
		}
		return session;
	}

	@Override
	public void saveSession(CacheHttpSession session)throws CacheSessionException {

		try {

			LOGGER.debug("########## CacheHttpSession saveSession [ID={},isNew={},isDiry={},isExpired={}]", session.getId(),

			session.isNew(), session.isDirty(), session.isExpired());

			memcachedSessionClient.set(super.sessionIdPrefix + session.getId(), session.getMaxInactiveInterval(), session);

		} catch (Exception e) {

			throw new CacheSessionException("################## CacheHttpSession saveSession error", e);
		}
	}

	@Override
	public void deleteSession(String sessionId) throws CacheSessionException{
		try {
			memcachedSessionClient.deleteWithNoReply(super.sessionIdPrefix + sessionId);

			LOGGER.debug("################## CacheHttpSession Delete [ID={}] success", sessionId);

		} catch (Exception e) {

			throw new CacheSessionException("################## CacheHttpSession Delete [ID=" + sessionId + "] error", e);
		}
	}

	@Override
	public void updateSession(final CacheHttpSession session) throws CacheSessionException{
		try {
			
			final GetsResponse<CacheHttpSession> response = memcachedSessionClient.gets(super.sessionIdPrefix + session.getId());
			
			LOGGER.debug("########## CacheHttpSession updateSession [ID={},isNew={},isDiry={},isExpired={}]", session.getId(), session.isNew(), session.isDirty(), session.isExpired());

			memcachedSessionClient.cas(super.sessionIdPrefix + session.getId(), session.getMaxInactiveInterval(), new CASOperation<CacheHttpSession>() {

				@Override
				public int getMaxTries() {
					return 6;
				}

				@Override
				public CacheHttpSession getNewValue(long currentCAS, CacheHttpSession currentValue) {
					if(response.getCas()==currentCAS){
						return session;
					}else{
						return currentValue;
					}
					
				}

			});
		} catch (Exception e) {

			throw new CacheSessionException("#########  CacheHttpSession updateSession [ID=" + session.getId() + "] error", e);
		}
	}

}