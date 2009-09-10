
all: clean compile

compile:
	mkdir build
	javac -Xlint -d build -cp lib/commons-lang-2.3.jar:lib/asm-3.1.jar:lib/commons-codec-1.3.jar:lib/commons-io-1.4.jar:lib/commons-logging-1.1.jar:lib/fontbox-0.1.0.jar:lib/icu4j-3.4.4.jar:lib/junit-3.8.1.jar:lib/log4j-1.2.14.jar:lib/lucene-core-2.1.0.jar:lib/lucene-highlighter-2.1.0.jar:lib/nekohtml-0.9.5.jar:lib/ooxml-lib/dom4j-1.6.1.jar:lib/ooxml-lib/jaxen-1.1.jar:lib/ooxml-lib/jsr173_1.0_api.jar:lib/ooxml-lib/ooxml-schemas.jar:lib/ooxml-lib/openxml4j-bin-alpha-080407.jar:lib/ooxml-lib/openxml4j-bin-beta-080728.jar:lib/ooxml-lib/openxml4j-src-alpha-080407.jar:lib/ooxml-lib/xercesImpl-2.8.1.jar:lib/ooxml-lib/xmlbeans-2.3.0.jar:lib/pdfbox-0.7.3.jar:lib/poi-3.5-beta5/poi-3.5-beta5.jar:lib/poi-3.5-beta5/poi-contrib-3.5-beta5.jar:lib/poi-3.5-beta5/poi-ooxml-3.5-beta5.jar:lib/poi-3.5-beta5/poi-scratchpad-3.5-beta5.jar:lib/tika-app-0.4.jar:lib/ws-commons-util-1.0.1.jar:lib/xmlrpc-common-3.0.jar:lib/xmlrpc-server-3.0.jar:lib/java_uno.jar:lib/jodconverter-2.2.2.jar:lib/juh-3.0.1.jar:lib/jurt-3.0.1.jar:lib/ridl-3.0.1.jar:lib/slf4j-api-1.5.6.jar:lib/slf4j-jdk14-1.5.6.jar:lib/unoil-3.0.1.jar:lib/xstream-1.3.1.jar \
	 src/com/knowledgetree/indexer/IndexerInterface.java src/com/knowledgetree/indexer/IndexerManager.java src/com/knowledgetree/indexer/QueryHit.java src/com/knowledgetree/metadata/KTMetaData.java src/com/knowledgetree/metadata/KTMetaDataInterface.java src/com/knowledgetree/openoffice/KTConverter.java src/com/knowledgetree/openoffice/ResourcePool.java src/com/knowledgetree/openoffice/KTConverterInterface.java src/com/knowledgetree/textextraction/KTTextExtractor.java src/com/knowledgetree/textextraction/KTTextExtractorInterface.java src/com/knowledgetree/textextraction/StringHandler.java src/com/knowledgetree/lucene/KTLuceneServer.java src/com/knowledgetree/lucene/KTLuceneServerInterface.java src/com/knowledgetree/lucene/TokenAuthenticationException.java
	jar cvfm ktlucene.jar MANIFEST.MF -C build/ .

install:
	cp ktlucene.jar /knowledgetree/bin/luceneserver

clean:
	rm -rf build ktlucene.jar bin


