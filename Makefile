
all: clean compile

compile:
	mkdir build
	javac -Xlint -d build -cp lib/commons-lang-2.3.jar:lib/commons-codec-1.3.jar:lib/commons-io-1.4.jar:lib/commons-logging-1.1.jar:lib/junit-3.8.1.jar:lib/log4j-1.2.14.jar:lib/lucene-core-2.1.0.jar:lib/lucene-highlighter-2.1.0.jar:lib/poi-3.6/poi-3.6.jar:lib/poi-3.6/poi-contrib-3.6.jar:lib/poi-3.6/poi-examples-3.6.jar:lib/poi-3.6/poi-ooxml-3.6.jar:lib/poi-3.6/poi-ooxml-schemas-3.6.jar:lib/poi-3.6/poi-scratchpad-3.6.jar:lib/poi-3.6/ooxml-lib/dom4j-1.6.1.jar:lib/poi-3.6/ooxml-lib/geronimo-stax-api_1.0_spec-1.0.jar:lib/poi-3.6/ooxml-lib/xmlbeans-2.3.0.jar:lib/tika-app-0.4.jar:lib/ws-commons-util-1.0.1.jar:lib/xmlrpc-common-3.0.jar:lib/xmlrpc-server-3.0.jar:lib/jodconverter-2.2.2.jar:lib/juh-3.0.1.jar:lib/jurt-3.0.1.jar:lib/ridl-3.0.1.jar:lib/slf4j-api-1.5.6.jar:lib/slf4j-jdk14-1.5.6.jar:lib/unoil-3.0.1.jar:lib/xstream-1.3.1.jar \
	 src/com/knowledgetree/indexer/IndexerInterface.java src/com/knowledgetree/indexer/IndexerManager.java src/com/knowledgetree/indexer/QueryHit.java src/com/knowledgetree/metadata/KTMetaData.java src/com/knowledgetree/metadata/KTMetaDataInterface.java src/com/knowledgetree/openoffice/KTConverter.java src/com/knowledgetree/openoffice/ResourcePool.java src/com/knowledgetree/openoffice/KTConverterInterface.java src/com/knowledgetree/textextraction/KTTextExtractor.java src/com/knowledgetree/textextraction/KTTextExtractorInterface.java src/com/knowledgetree/textextraction/StringHandler.java src/com/knowledgetree/lucene/KTLuceneServer.java src/com/knowledgetree/lucene/KTLuceneServerInterface.java src/com/knowledgetree/lucene/TokenAuthenticationException.java
	jar cvfm ktlucene.jar MANIFEST.MF -C build/ .

install:
	cp ktlucene.jar /knowledgetree/bin/luceneserver

clean:
	rm -rf build ktlucene.jar bin


