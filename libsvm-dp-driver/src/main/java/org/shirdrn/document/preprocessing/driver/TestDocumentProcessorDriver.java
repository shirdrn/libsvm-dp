package org.shirdrn.document.preprocessing.driver;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.common.Component;
import org.shirdrn.document.preprocessing.common.VectorMetadataImpl;
import org.shirdrn.document.preprocessing.component.BasicInformationCollector;
import org.shirdrn.document.preprocessing.component.DocumentTFIDFComputation;
import org.shirdrn.document.preprocessing.component.DocumentWordsCollector;
import org.shirdrn.document.preprocessing.component.test.LoadFeatureTermVector;
import org.shirdrn.document.preprocessing.component.test.OutputtingQuantizedTestData;
import org.shirdrn.document.preprocessing.driver.common.AbstractDocumentProcessorDriver;

/**
 * The driver for starting components to process TEST data set.
 * It includes the following 5 components:
 * <ol>
 * <li>{@link BasicInformationCollector}</li>
 * <li>{@link DocumentWordsCollector}</li>
 * <li>{@link LoadFeatureTermVector}</li>
 * <li>{@link DocumentTFIDFComputation}</li>
 * <li>{@link OutputtingQuantizedTestData}</li>
 * </ol>
 * Executing above components in order can output the normalized
 * data for feeding libSVM classifier developed by <code>Lin Chih-Jen</code>
 * (<a href="www.csie.ntu.edu.tw/~cjlin/libsvm/‎">www.csie.ntu.edu.tw/~cjlin/libsvm/‎</a>)
 * 
 * @author Shirdrn
 */
public class TestDocumentProcessorDriver extends AbstractDocumentProcessorDriver {

	@Override
	public void process() {
		Context context = super.newContext(ProcessorType.TRAIN, "config-test.properties");
		context.setVectorMetadata(new VectorMetadataImpl());
		// for test data
		Component[]	chain = new Component[] {
				new BasicInformationCollector(context),
				new DocumentWordsCollector(context),
				new LoadFeatureTermVector(context),
				new DocumentTFIDFComputation(context),
				new OutputtingQuantizedTestData(context)
			};
		run(chain);
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(
				TestDocumentProcessorDriver.class);		
	}

}
