 
all: clean compile

compile:  
	mkdir build 
	javac -d build -cp lib/xmlrpc-server-3.0.jar:lib/xmlrpc-common-3.0.jar:lib/lucene-core-2.1.0.jar:lib/lucene-highlighter-2.1.0.jar:lib/log4j-1.2.14.jar:lib/commons-lang-2.3.jar:lib/tika-0.2-SNAPSHOT-standalone.jar src/com/knowledgetree/lucene/IndexerInterface.java src/com/knowledgetree/lucene/IndexerManager.java src/com/knowledgetree/lucene/QueryHit.java src/com/knowledgetree/metadata/KTMetaData.java src/com/knowledgetree/metadata/KTMetaDataInterface.java src/com/knowledgetree/textextraction/KTTextExtractor.java src/com/knowledgetree/textextraction/KTTextExtractorInterface.java src/com/knowledgetree/textextraction/StringHandler.java src/com/knowledgetree/xmlrpc/KTXmlRpcServer.java src/com/knowledgetree/xmlrpc/KTXmlRpcServerInterface.java src/com/knowledgetree/xmlrpc/TokenAuthenticationException.java
	jar cvfm ktlucene.jar MANIFEST.MF -C build/ .

install: 
	cp ktlucene.jar /knowledgetree/ktdms.trunk/bin/luceneserver

clean:
	rm -rf build ktlucene.jar bin
