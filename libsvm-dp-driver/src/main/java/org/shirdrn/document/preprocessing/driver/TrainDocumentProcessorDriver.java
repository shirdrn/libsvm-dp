package org.shirdrn.document.preprocessing.driver;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.component.BasicInformationCollector;
import org.shirdrn.document.preprocessing.component.DocumentTFIDFComputation;
import org.shirdrn.document.preprocessing.component.DocumentWordsCollector;
import org.shirdrn.document.preprocessing.component.train.FeaturedTermVectorSelector;
import org.shirdrn.document.preprocessing.component.train.OutputtingQuantizedTrainData;
import org.shirdrn.document.preprocessing.driver.common.AbstractDocumentProcessorDriver;
import org.shirdrn.document.preprocessing.utils.PreprocessingUtils;

/**
 * The driver for starting components to process TRAIN data set.
 * It includes the following 5 components:
 * <ol>
 * <li>{@link BasicInformationCollector}</li>
 * <li>{@link DocumentWordsCollector}</li>
 * <li>{@link FeaturedTermVectorSelector}</li>
 * <li>{@link DocumentTFIDFComputation}</li>
 * <li>{@link OutputtingQuantizedTrainData}</li>
 * </ol>
 * Executing above components in order can output the normalized
 * data for feeding libSVM classifier developed by <code>Lin Chih-Jen</code>
 * (<a href="www.csie.ntu.edu.tw/~cjlin/libsvm/‎">www.csie.ntu.edu.tw/~cjlin/libsvm/‎</a>)</br>
 * It can produce 2 files represented by the specified properties:
 * <ol>
 * <li>a term vector file property: <code>processor.dataset.chi.term.vector.file</code></li>
 * <li>a label vector file property: <code>processor.dataset.label.vector.file</code></li>
 * </ol>
 * which are used by {@link TestDocumentProcessorDriver} to produce TEST vector data.
 * 
 * @author Shirdrn
 */
public class TrainDocumentProcessorDriver extends AbstractDocumentProcessorDriver {

	private static final String CONFIG = "config-train.properties";
	
	@Override
	public void process() {
		Context context = super.newContext(ProcessorType.TRAIN, CONFIG);
		context.setVectorMetadata(PreprocessingUtils.newVectorMetadata());
		
		// build component chain for train data
		Class<?>[] classes = new Class[] {
				BasicInformationCollector.class,
				DocumentWordsCollector.class,
				FeaturedTermVectorSelector.class,
				DocumentTFIDFComputation.class,
				OutputtingQuantizedTrainData.class
		};
		
		run(PreprocessingUtils.newChainedComponents(context, classes));
	}
	
	public static void main(String[] args) {
		AbstractDocumentProcessorDriver.start(TrainDocumentProcessorDriver.class);	
	}

}
