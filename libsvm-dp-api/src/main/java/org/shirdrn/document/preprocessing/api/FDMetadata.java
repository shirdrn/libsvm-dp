package org.shirdrn.document.preprocessing.api;

import java.io.File;

public interface FDMetadata {

	File getInputRootDir();

	File getOutputDir();

	String getOutputVectorFile();

	String getFileExtensionName();

	File getLabelVectorFile();

	File getFeatureTermVectorFile();

}
