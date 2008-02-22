 
all: clean compile

compile:  
	mkdir build 
	javac -d build -cp lib/xmlrpc-server-3.0.jar:lib/xmlrpc-common-3.0.jar:lib/lucene-core-2.1.0.jar:lib/lucene-highlighter-2.1.0.jar:lib/log4j-1.2.14.jar:lib/commons-lang-2.3.jar src/com/knowledgetree/lucene/KTLuceneServer.java src/com/knowledgetree/lucene/core/IndexerInterface.java src/com/knowledgetree/lucene/core/IndexerManager.java src/com/knowledgetree/lucene/core/QueryHit.java src/com/knowledgetree/lucene/core/TokenAuthenticationException.java
	jar cvfm ktlucene.jar MANIFEST.MF -C build/ .

install: 
	cp ktlucene.jar /knowledgetree/ktdms.trunk/bin/luceneserver

clean:
	rm -rf build ktlucene.jar bin
