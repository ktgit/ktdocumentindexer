/**
 *
 * Exposes XMLRPC functionality contained within.
 *
 * @license
 *
 */

package com.knowledgetree.indexer;

import com.knowledgetree.lucene.TokenAuthenticationException;

// TODO: ktid has been added for future use!
// idea is that KT Live can pass ktid, which would mean one indexing server could manage multiple
// kt installations, but the indexes are managed based on the ktid.

public class IndexerInterface 
{
	/**
	 * get some basic statistics from Lucene
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getStatistics(String ktid, String token) throws Exception
	{		
		IndexerManager manager = IndexerManager.get();
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		return manager.getStatistics();
	}
	
	/**
	 * check if the document exists 
	 * 
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public boolean documentExists(String ktid,String token, int documentId) throws Exception
 	{
 		IndexerManager manager = IndexerManager.get();
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
 		try 
 		{
			return manager.documentExists(documentId);
		} 
 		catch (Exception ex) 
 		{
 			manager.getLogger().error(ex.getMessage());
 			return false;
		}
 	}
	
	/**
	 * adds the document to the 'internal queue	
	 * 
	 * @param documentId
	 * @param contentFilename
	 * @param propertiesFilename
	 * @return
	 * @throws Exception
	 */
	public int addDocument(String ktid,String token,int documentId, String contentFilename, String discussion, String title, String version) throws Exception
	{
		IndexerManager manager = IndexerManager.get();		 
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		try
		{
			manager.getLogger().debug("addDocument("+documentId + "," + contentFilename + ",...)");

			manager.indexDocument(documentId, contentFilename, discussion, title, version);
		}
		catch(Exception ex)
		{
			manager.getLogger().error(ex.getMessage());
			return -1;
		}
		
		return 0;
	}

	/**
	 * Update the discussion on a document
	 * 
	 * @param documentId
	 * @param discussion
	 * @return int
	 * @throws Exception
	 */
	public int updateDiscussion(String ktid,String token,int documentId, String discussion) throws Exception
	{
		IndexerManager manager = IndexerManager.get();	
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		try
		{
			manager.getLogger().debug("updateDiscussion("+documentId + ",...)");

			manager.updateDiscussion(documentId, discussion);
		}
		catch(Exception ex)
		{
			manager.getLogger().error(ex.getMessage());
			return -1;
		}
		
		return 0;
	}
	
	
	

	/**
	 * remove the document from Lucene
	 * 
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public int deleteDocument(String ktid,String token,int documentId) throws Exception
	{
		IndexerManager manager = IndexerManager.get();
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		manager.getLogger().debug("deleteDocument("+documentId + ")");

		try 
		{			
			manager.deleteDocument(documentId);
		} 
		catch (Exception ex) 
		{
			manager.getLogger().error(ex.getMessage());
			return -1;			
		}
		
		return 0;
	}
	
	/**
	 * optimise the lucene index
	 * 
	 * @return
	 * @throws Exception
	 */
	public int optimise(String ktid,String token) throws Exception
	{
		IndexerManager manager = IndexerManager.get();
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		manager.getLogger().debug("optimise()");
		try 
		{			
			manager.optimise();
			manager.getLogger().debug("optimise() done");
		} 
		catch (Exception ex) 
		{
			manager.getLogger().error(ex.getMessage());
			return -1;			
		}
		
		return 0;
	}	
	
	/**
	 * run a query through lucene.
	 * returns a JSON result structure as it is really very easy to parse!
	 * 
	 * @param query
	 * @return string
	 * @throws Exception
	 */
	public String query(String ktid,String token,String query) throws Exception
	{
		IndexerManager manager = IndexerManager.get();
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		manager.getLogger().debug("query("+query+")");
		try 
		{
			
			QueryHit[] docs = manager.query(query, true);
			
			return QueryHit.toJSON(docs);
		} 
		catch (Exception ex) 
		{
			manager.getLogger().error(ex.getClass().getName() + ":" + ex.getMessage());
			return null;			
		}		
	}
	
	/**
	 * Returns the text for a specific document.
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public String getText(String ktid,String token,int documentId) throws Exception
	{
		IndexerManager manager = IndexerManager.get();
		
		if (!manager.authenticate(token))
		{
			throw new TokenAuthenticationException(token);
		}
		
		manager.getLogger().debug("getText("+documentId+")");
		
		String result = manager.getText(documentId);
		
		return result;
	}
	
}