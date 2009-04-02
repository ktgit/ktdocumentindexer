/**
 *
 * The basic result structure that can be JSONified.
 *
 * @license
 *
 */

package com.knowledgetree.indexer;

import java.io.Serializable;

import org.apache.commons.lang.StringEscapeUtils;

@SuppressWarnings("serial")
public class QueryHit implements Serializable
{
	public long 	DocumentID;
	public float 	Rank;
	public String	Title;
	public String	Content;
	public String	Version;
	
	public static String toJSON(QueryHit[] docs) throws Exception 
	{
		String jsonBuilder = "[";
		
		for(int i=0;i<docs.length;i++)
		{
			if (i>0) 
			{
				jsonBuilder += ",";
			}
			
			QueryHit doc = docs[i];
			
			String title= (doc.Title==null)?"":doc.Title;
			String content = (doc.Content==null)?"":doc.Content;
			String version = (doc.Version==null)?"":doc.Version;
			
			jsonBuilder
				+= "{" 
				+ "\"DocumentID\":" + doc.DocumentID + "," 
				+ "\"Rank\":" + doc.Rank + "," 
				+ "\"Title\":\"" + StringEscapeUtils.escapeJava(title) + "\"," 
				+ "\"Version\":\"" + StringEscapeUtils.escapeJava(version)+"\"," 
				+ "\"Content\":\"" + StringEscapeUtils.escapeJava(content)+"\"" 
				+"}";					
		}
		jsonBuilder += "]";
		
		IndexerManager manager = IndexerManager.get();
		manager.getLogger().debug("found: " + jsonBuilder);
		
		return jsonBuilder;
	}
}
