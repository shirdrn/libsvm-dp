package org.shirdrn.document.preprocessing.api;

public interface Context {
	
	String getCharset();
	
	FDMetadata getFDMetadata();

	ConfigReadable getConfiguration();
	
	VectorMetadata getVectorMetadata();
	
	void setVectorMetadata(VectorMetadata vectorMetadata);

	ProcessorType getProcessorType();

}
