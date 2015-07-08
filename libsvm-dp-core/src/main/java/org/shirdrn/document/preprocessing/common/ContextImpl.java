package org.shirdrn.document.preprocessing.common;

import org.shirdrn.document.preprocessing.api.ConfigReadable;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.FDMetadata;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.api.VectorMetadata;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;

public class ContextImpl implements Context {

	private final ConfigReadable configuration;
	private VectorMetadata vectorMetadata;
	private final FDMetadata fDMetadata;
	private final ProcessorType processorType;
	
	public ContextImpl(ProcessorType processorType, String config) {
		this.processorType = processorType;
		this.configuration = new Configuration("config.properties");
		((Configuration) this.configuration).addResource(config);
		this.fDMetadata = new FDMetadataImpl(processorType, configuration);
	}
	
	public ContextImpl() {
		this.processorType = ProcessorType.TRAIN;
		this.configuration = new Configuration();
		this.fDMetadata = new FDMetadataImpl(processorType, configuration);
	}
	
	@Override
	public FDMetadata getFDMetadata() {
		return fDMetadata;
	}

	@Override
	public ConfigReadable getConfiguration() {
		return configuration;
	}
	
	@Override
	public VectorMetadata getVectorMetadata() {
		return vectorMetadata;
	}
	
	@Override
	public void setVectorMetadata(VectorMetadata vectorMetadata) {
		this.vectorMetadata = vectorMetadata;
	}

	@Override
	public ProcessorType getProcessorType() {
		return processorType;
	}

	@Override
	public String getCharset() {
		return configuration.get(ConfigKeys.DATASET_FILE_CHARSET, "UTF-8");
	}

}
