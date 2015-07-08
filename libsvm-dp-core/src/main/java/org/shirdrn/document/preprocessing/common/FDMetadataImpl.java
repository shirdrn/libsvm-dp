package org.shirdrn.document.preprocessing.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.ConfigReadable;
import org.shirdrn.document.preprocessing.api.FDMetadata;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.utils.CheckUtils;

public class FDMetadataImpl implements FDMetadata {

	private static final Log LOG = LogFactory.getLog(FDMetadata.class);
	private final File inputRootDir;
	private final File outputDir;
	private final String outputVectorFile;
	private final String fileExtensionName;
	private final File labelVectorFile;
	private final File featureTermVectorFile;
	
	public FDMetadataImpl(ProcessorType processorType, ConfigReadable configuration) {
		// initialize
		fileExtensionName = configuration.get(ConfigKeys.DATASET_TRAIN_FILE_EXTENSION, "");
		LOG.info("Train dataset file extension: name=" + fileExtensionName);
		String termsFile = configuration.get(ConfigKeys.DATASET_FEATURE_TERM_VECTOR_FILE);
		CheckUtils.checkNotNull(termsFile);
		featureTermVectorFile = new File(termsFile);
		
		if(processorType == ProcessorType.TRAIN) {
			String trainInputRootDir = configuration.get(ConfigKeys.DATASET_TRAIN_INPUT_ROOT_FILE);
			String train = configuration.get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_FILE);
			String trainOutputDir = configuration.get(ConfigKeys.DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR);
			inputRootDir = new File(trainInputRootDir);
			outputVectorFile = train;
			outputDir = new File(trainOutputDir);
			
			// check existence: 
			// parent directory of term file MUST exist
			CheckUtils.checkFile(featureTermVectorFile.getParentFile(), false);
			// term file MUST NOT exist
			CheckUtils.checkFile(featureTermVectorFile, true);			
		} else if(processorType == ProcessorType.TEST) {
			String testInputRootDir = configuration.get(ConfigKeys.DATASET_TEST_INPUT_ROOT_FILE);
			String test = configuration.get(ConfigKeys.DATASET_TEST_SVM_VECTOR_FILE);
			String testOutputDir = configuration.get(ConfigKeys.DATASET_TEST_SVM_VECTOR_OUTPUT_DIR);
			inputRootDir = new File(testInputRootDir);
			outputVectorFile = test;
			outputDir = new File(testOutputDir);
			
			CheckUtils.checkFile(featureTermVectorFile, false);
		} else {
			throw new RuntimeException("Undefined processor type!");
		}
		
		String labels = configuration.get(ConfigKeys.DATASET_LABEL_VECTOR_FILE);
		labelVectorFile = new File(labels);
		
		LOG.info("Vector input root directory: outputDir=" + inputRootDir);
		LOG.info("Vector output directory: outputDir=" + outputDir);
		LOG.info("Vector output file: outputFile=" + outputVectorFile);
	}

	@Override
	public File getInputRootDir() {
		return inputRootDir;
	}
	
	@Override
	public File getOutputDir() {
		return outputDir;
	}

	@Override
	public String getOutputVectorFile() {
		return outputVectorFile;
	}

	@Override
	public String getFileExtensionName() {
		return fileExtensionName;
	}

	@Override
	public File getLabelVectorFile() {
		return labelVectorFile;
	}

	@Override
	public File getFeatureTermVectorFile() {
		return featureTermVectorFile;
	}

}
