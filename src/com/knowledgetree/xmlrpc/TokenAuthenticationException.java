package com.knowledgetree.xmlrpc;

import com.knowledgetree.lucene.IndexerManager;

@SuppressWarnings("serial")
public class TokenAuthenticationException extends Exception {
	
	public TokenAuthenticationException(String token)
	{
		try 
		{
			IndexerManager manager = IndexerManager.get();
			manager.getLogger().debug("TokenAuthenticationException: " + token);
		} 
		catch (Exception e) 
		{
		}
	}

}
