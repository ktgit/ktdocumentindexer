package com.knowledgetree.xmlrpc;


	public class KTXmlRpcServerInterface {
	
	/**
	 * Shut the server down.
	 * 
	 * @return int
	 * @throws Exception
	 */
	public int shutdown(String ktid,String token) throws Exception  
	{
		KTXmlRpcServer manager = KTXmlRpcServer.get();		
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		try
		{ 
			manager.shutdown();
		}
		catch(Exception ex)
		{
			manager.getLogger().error(ex.getMessage());
			return -1;
		}
		
		return 0;
	}
}
