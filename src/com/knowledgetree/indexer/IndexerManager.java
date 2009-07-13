/**
 *
 * The index manager controls the lucene indexing system.
 *
 * @license
 *
 */

package com.knowledgetree.indexer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.beans.Beans;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.lucene.search.TermQuery;

import com.knowledgetree.lucene.KTLuceneServer;

public class IndexerManager implements Formatter
{

	public static final String KnowledgeTreeLoggingProperties = "KnowledgeTreeIndexer.Logging.properties";
	private static IndexerManager 		indexingManager;
	private IndexReader 				queryReader;
	private Searcher 					querySearcher;
	private Analyzer 					analyzer;
	private ReentrantReadWriteLock 		locker;
	private Logger 						logger;
	private String 						indexDirectory 		= "../../../var/indexes";
	private String						propertiesFilename 	= "KnowledgeTreeIndexer.properties";
	private String 						clientIps 			= "127.0.0.1";
	private int							maxQueryResult		= 1000;
	private Date						startDate;
	private	int							documentsAddCount	= 0;
	private	int							documentsDeleteCount	= 0;
	private	int							queryCount		= 0;
	private	int							optimiseCount		= 0;
	private	int							resultFragments		= 3;
	private String						resultSeperator		= "...";
	private	int							resultFragmentSize	= 40;
	
	// basic getter() functions	
	public Logger getLogger()			{ return logger; }
	
	/**
	 * Indicates if the authentication token matches
	 * 
	 * @param hash
	 * @return
	 */
	public boolean authenticate(String token)
	{
		return KTLuceneServer.get().authenticate(token); 
	}
	
	/**
	 * Returns a reference to a singleton of the IndexerManager.
	 * @return IndexerManager
	 * @throws Exception
	 */
	public static IndexerManager get() throws Exception 
	{		
		if (null == IndexerManager.indexingManager) 
		{
			IndexerManager.indexingManager = new IndexerManager();			
		}
		return IndexerManager.indexingManager;
	}

	
	
	/**
	 * Returns the statistics on the indexer. The result is a JSONified string.
	 * 
	 * @return String
	 */
	public String getStatistics()
	{
		StringBuilder jsonBuilder = new StringBuilder();
		
		int numDocs = this.queryReader.numDocs();
		
		jsonBuilder
			.append('{')
			.append("\"dateStarted\":\"").append(this.startDate).append("\",")
			.append("\"dateNow\":\"").append(new Date()).append("\",")
			.append("\"indexDirectory\":\"").append(this.indexDirectory).append("\",")
			.append("\"queryResultMax\":").append(this.maxQueryResult).append(",")
			.append("\"countAdded\":").append(this.documentsAddCount).append(",")
			.append("\"countDeleted\":").append(this.documentsDeleteCount).append(",")
			.append("\"countOptimised\":").append(this.optimiseCount).append(",")
			.append("\"countQuery\":").append(this.queryCount).append(",")
			.append("\"countDocuments\":").append(numDocs)
			.append('}'); 
		
		return jsonBuilder.toString();
	}
	
	/**
	 * Gets analyzers from xml configuration file.
	 * @throws XPathExpressionException 
	 */
	private Analyzer getAnalyzer(String analyzerClass) throws Exception {
		Analyzer retval = null;
		Object bean = Beans.instantiate(getClass().getClassLoader(), analyzerClass);
		if (Beans.isInstanceOf(bean, Analyzer.class)) {
			retval = (Analyzer) Beans.getInstanceOf(bean, Analyzer.class);
		}
		return retval;	
	}
	/**
	 * Constructor for IndexerManager.
	 * @throws Exception
	 */
	private IndexerManager() throws Exception 
	{
		this.logger  = Logger.getLogger("com.knowledgetree.lucene");		 
		this.logger.info("Indexer starting up...");

		//this.analyzer = new StandardAnalyzer();
		this.locker = new ReentrantReadWriteLock();
		this.startDate = new Date();
		
		// load properties
		this.logger.info("Loading properties file: " + this.propertiesFilename);
		Properties properties = new Properties();
		try
		{
			FileInputStream in = new FileInputStream(this.propertiesFilename);
			properties.load(in);
			in.close();
		}
		catch(Exception ex)
		{
			this.logger.error("Problem loading properties: " + ex.getMessage());
			throw ex;
		}
		
		this.analyzer = getAnalyzer(properties.getProperty("indexer.analyzer"));

		// test that the index folder exists and is writable
		this.indexDirectory = properties.getProperty("indexer.directory", this.indexDirectory);
		this.logger.info("Using index directory: " + this.indexDirectory);
		File dir = new File(this.indexDirectory);
		if (!dir.isDirectory())
		{
			throw new Exception("Invalid index directory specified: " + this.indexDirectory);
		}
		if (!dir.canWrite() || !dir.canRead())
		{
			throw new Exception("Index directory must be read and writable: " + this.indexDirectory);
		}
		
		
		this.maxQueryResult = Integer.parseInt(properties.getProperty("query.max.results", Integer.toString(this.maxQueryResult)));
		this.resultFragments = Integer.parseInt(properties.getProperty("result.fragments", Integer.toString(this.resultFragments)));
		this.resultSeperator = properties.getProperty("result.fragment.seperator", this.resultSeperator);
		this.resultFragmentSize = Integer.parseInt(properties.getProperty("result.fragment.size", Integer.toString(this.resultFragmentSize)));

		this.logger.info("Starting: " + this.startDate);
		this.logger.info("Client IPs: " + this.clientIps);
		this.logger.info("Max query result: " + this.maxQueryResult);
		this.logger.info("Result fragments: " + this.resultFragments);
		this.logger.info("Result fragment seperator: " + this.resultSeperator);
		this.logger.info("Result fragment size: " + this.resultFragmentSize);
				
		// open the index
		try
		{
			this.reopenIndex();
		}
		catch(FileNotFoundException ex)
		{
			String msg = ex.getMessage();
			 
			if (msg.indexOf("no segments* file found") == 0)
			{
				this.logger.info("Suspect that this is first time that indexing is run. Will attempt to create segments in " + this.indexDirectory);
				this.create();
				this.reopenIndex();
			}
			else
			{
				throw ex;
			}
		}
	}

	/**
	 * Closes any existing readers and reopens them.
	 * @throws Exception
	 */
	private void reopenIndex() throws Exception 
	{
		this.logger.debug("Reopenning index");
		WriteLock lock = this.locker.writeLock();
		lock.lock();
		try
		{
			if (null != this.queryReader)
			{
				this.querySearcher.close();
				this.queryReader.close();
			}
			this.queryReader = IndexReader.open(this.indexDirectory);
			this.querySearcher = new IndexSearcher(this.queryReader);		
			this.logger.debug("Timestamp: " + new Date());
			this.logger.debug("Documents in index: " + this.queryReader.numDocs());
		}
		finally
		{
			lock.unlock();
		}
	}
	
	// some basic conversion helper structures
	final static char numc[] = {'0','1','2','3','4','5','6','7','8','9'};
	final static char alphac[] = {'a','b','c','d','e','f','g','h','i','j'};	
	
	/**
	 * Convert a long to a string
	 * @param longv
	 * @return String
	 */
	public static String longToString(long longv)
	{
		String s = Long.toString(longv);
		
		for(int i=0;i<10;i++)
		{
			s = s.replace(numc[i], alphac[i]);
		}
		
		return s;
	}
	
	/**
	 * Convert a string to a long
	 * @param sv
	 * @return long
	 */
	public static long stringToLong(String sv)
	{		
		for(int i=0;i<10;i++)
		{
			sv = sv.replace(alphac[i], numc[i]);
		}
		
		return Long.parseLong(sv);	
	}	
	
	/**
	 * Identifies if the document has been indexed.
	 * @param documentId
	 * @return boolean
	 * @throws IOException 
	 */
	public boolean documentExists(int documentId) throws IOException
	{		
		QueryParser parser=new QueryParser("DocumentID", this.analyzer);
		
		ReadLock lock = this.locker.readLock();
		lock.lock();		 
		try
		{			
			try 
			{
				Query query = new TermQuery(new Term("DocumentID",IndexerManager.longToString(documentId))); 

				query=query.rewrite(this.queryReader);
 
				// run the search!
				Hits hits = this.querySearcher.search(query);
				boolean found = (hits.length() > 0);
				this.logger.debug("Checking document exists documentId=" +documentId + " result="+found);
				return found;
			} 
			catch (IOException ex) 
			{
				throw ex;
			}
		}
		finally
		{
			lock.unlock();
		}				
	}
	
	/**
	 * Delete a document contained within the lucene index
	 * 
	 * @param documentId
	 * @throws Exception
	 */
	public void deleteDocument(int documentId) throws  Exception  
	{
		synchronized (this) 
		{
			this.documentsDeleteCount++;
		}
		
		this.logger.debug("Deleting document: " + documentId);
		IndexReader reader = IndexReader.open(this.indexDirectory);
		int deleted = reader.deleteDocuments(new Term ("DocumentID", IndexerManager.longToString(documentId)));
		reader.close();
		this.logger.debug("Deleted " + deleted + " documents.");
		
		this.reopenIndex();
	}

	public void create() throws Exception
	{
		IndexWriter writer = new IndexWriter(this.indexDirectory, this.analyzer, true);
		writer.close();
	}
	
	/**
	 * Optimise the lucene database.
	 * @throws Exception
	 */
	public void optimise() throws Exception 
	{
		synchronized (this)
		{
			this.optimiseCount++;
		}		
		
		this.logger.debug("Optimise index");
		WriteLock lock = this.locker.writeLock();
		lock.lock();
		try
		{
			if (null != this.queryReader)
			{
				this.querySearcher.close();
				this.queryReader.close();
			}
			
			IndexWriter writer = new IndexWriter(this.indexDirectory, this.analyzer, false);
			writer.optimize();
			writer.close();
						
			this.queryReader = IndexReader.open(this.indexDirectory);
			this.querySearcher = new IndexSearcher(this.queryReader);		
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Pass a query to the database. This by default uses a maximum set of results.
	 * @param queryString
	 * @return QueryHit[]
	 * @throws Exception
	 */
	public QueryHit[] query(String queryString) throws Exception
	{
		return this.query(queryString, this.maxQueryResult, false);
	}	
	
	/**
	 * Pass a query to the database. This by default uses a maximum set of results.
	 * @param queryString
	 * @param getText
	 * @return QueryHit[]
	 * @throws Exception
	 */
	public QueryHit[] query(String queryString, boolean getText) throws Exception
	{
		return this.query(queryString, this.maxQueryResult, getText);
	}
	
	/**
	 * Returns a set of hits from lucene.
	 * @param queryString
	 * @param maxHits
	 * @return
	 * @throws Exception
	 */
	public QueryHit[] query(String queryString, int maxHits, boolean getText) throws Exception 
	{
		synchronized (this)
		{
			this.queryCount++;
		}		 
		
		String tmp = queryString.toLowerCase();
		boolean queryContent = tmp.indexOf("content") != -1;
		boolean queryDiscussion = tmp.indexOf("discussion") != -1;
		 
		QueryParser parser=new QueryParser("Content", this.analyzer);
		Query query = parser.parse(queryString);
	 
		// rewriting is important for complex queries. this is a must-do according to sources!
		query=query.rewrite(this.queryReader);
		
		// run the search!
		Hits hits = this.querySearcher.search(query);
		
		// now we can apply the maximum hits to the results we return!
		int max = (maxHits == -1)?hits.length():maxHits;
		
		if (hits.length() < max) 
		{
			max = hits.length();
		}
		
		QueryHit[] results = new QueryHit[max];				
		
		Highlighter highlighter =new Highlighter( this,new QueryScorer(query));
		highlighter.setTextFragmenter(new SimpleFragmenter(this.resultFragmentSize));		
		for (int i = 0; i < max; i++)
		{
			Document doc = hits.doc(i);			
			 
			QueryHit hit = new QueryHit();
			hit.DocumentID = IndexerManager.stringToLong(doc.get("DocumentID"));
			hit.Rank = hits.score(i);
			hit.Title = doc.get("Title");
			if (getText)
			{
				String text = "";
				if (queryContent)
				{
					text +=  doc.get("Content");
				}
				if (queryDiscussion)
				{
					text +=  doc.get("Discussion");
				}
						
				// TODO: we can create a field.getReader(). the fragmenting needs to
				// be updated to deal with the reader only. would prefer not having to
				// load the document into a string!
				TokenStream tokenStream=analyzer.tokenStream("contents", new StringReader(text));

				hit.Content = highlighter.getBestFragments(tokenStream,text,this.resultFragments,this.resultSeperator);						
			}
			else
			{
				hit.Content = "";			
			}
			
			hit.Version = doc.get("Version");
						
			results[i] = hit;			
		}
		
		return results;
	}
	
	/**
	 * Get text for a given document
	 * 
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public String getText(int documentId) throws Exception
	{
		QueryHit[] results = this.query("DocumentID:" + IndexerManager.longToString(documentId), true);
		
		return QueryHit.toJSON(results);
	}
	
	/**
	 * Starts the indexing process.
	 * 
	 * @param documentId
	 * @param contentFilename
	 * @param discussion
	 * @param version
	 * @throws Exception 
	 */
	public void indexDocument(int documentId, String contentFilename, String discussion, String title, String version) throws Exception 
	{
		synchronized (this)
		{
			this.documentsAddCount++;
		}		
		
		this.logger.debug("Indexing document: documentid=" + documentId);

		// remove an existing document, if it exists. lucene doesn't do this for us!
		this.deleteDocument(documentId);

		File contentFile = new File(contentFilename);
		long filesize = contentFile.length();
		byte buf[] = new byte[(int) filesize];
		 
		DataInputStream dis = new DataInputStream(new FileInputStream(contentFilename));
		dis.read(buf, 0, (int) filesize); 
		dis.close();
		
		String content=new java.lang.String(buf, "UTF-8");
		
		this.addLuceneDocument(documentId, content, discussion, title, version);
			
		// delete the temporary file  
		contentFile.delete();
	}
	
	/**
	 * This adds a lucene document
	 * 
	 * @param documentId
	 * @param content
	 * @param discussion
	 * @param title
	 * @param version
	 * @throws Exception 
	 */
	private void addLuceneDocument(int documentId, String content, String discussion, String title, String version) throws Exception
	{
		// create the lucene document
		 
		Document document = new Document();		
		document.add(new Field("DocumentID", IndexerManager.longToString(documentId), Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("Content", content, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("Discussion", discussion, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("Title", title, Field.Store.YES, Field.Index.TOKENIZED));
		document.add(new Field("Version", version, Field.Store.YES, Field.Index.UN_TOKENIZED));

		// add the document to lucene index
		try 
		{
			this.logger.debug("Opening index writer: documentid=" + documentId);
			this.logger.debug("DocumentID: " + IndexerManager.longToString(documentId));
			this.logger.debug("Content: " + content);
			this.logger.debug("Discussion: " + discussion);
			IndexWriter writer = new IndexWriter(this.indexDirectory, this.analyzer, false);
			writer.addDocument(document);
			writer.close();
			this.logger.debug("Closing index writer: documentid=" + documentId);			
		} 
		catch (IOException ex) 
		{
			logger.error("Problem indexing document: documentid=" + documentId + " with exception: " + ex.getMessage());
		}	
		
		this.reopenIndex();		
	}
	
	/**
	 * Update the discussion on a document.
	 * @param documentId
	 * @param discussion
	 * @throws Exception
	 */
	public void updateDiscussion(int documentId, String discussion) throws Exception 
	{
		this.logger.debug("updateDiscussion: documentid=" + documentId);
		QueryParser parser=new QueryParser("DocumentID", this.analyzer);
		Query query = new TermQuery(new Term("DocumentID",IndexerManager.longToString(documentId))); 

		query=query.rewrite(this.queryReader);
 
		// run the search!
		Hits hits = this.querySearcher.search(query);
		boolean found = false;
		
		for (int i = 0; i < hits.length(); i++)
		{
			Document doc = hits.doc(i);	
			
			String content = doc.get("Content");
			String title = doc.get("Title");
			String version = doc.get("Version");
			
			this.deleteDocument(documentId);
			this.addLuceneDocument(documentId, content, discussion, title, version);
			found = true;
			
			break; // there shouldn't be others...
		}
		if (!found)
		{
			// there is no content
			this.addLuceneDocument(documentId, "", discussion, "", "");
		}	
	}
	
	
	
	public String highlightTerm(String originalText , TokenGroup group)
	{
		if (group.getTotalScore() <= 0)
		{
			return originalText;
		}
		 
		return "<b>" + originalText + "</b>";
	}
	 
}
