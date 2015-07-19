package org.shirdrn.document.preprocessing.component.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.component.AbstractOutputtingQuantizedData;
import org.shirdrn.document.preprocessing.utils.FileUtils;

import com.google.common.collect.Maps;

/**
 * Give a unique id to a unique term or label respectively, and then output
 * the mapping from text word to number id to a file, because the libSVM
 * require quantized data lines from input files.
 * 
 * @author Shirdrn
 */
public class OutputtingQuantizedTrainData extends AbstractOutputtingQuantizedData {

	private static final Log LOG = LogFactory.getLog(OutputtingQuantizedTrainData.class);
	private int labelNumber = 0;
	private int wordNumber = 0;
	
	public OutputtingQuantizedTrainData(final Context context) {
		super(context);
	}

	@Override
	public void fire() {
		super.fire();
		LOG.info("Output label mapping file: labelFile=" + context.getFDMetadata().getLabelVectorFile());
		output(context.getFDMetadata().getLabelVectorFile(), context.getVectorMetadata().labelVectorMapIterator());
	}
	
	private void output(File file, Iterator<Entry<String, Integer>> iter) {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), context.getCharset()));
			while(iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				w.write(entry.getValue().toString() + " " + entry.getKey());
				w.newLine();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeQuietly(w);
		}
	}
	
	
	@Override
	protected void quantizeTermVectors() {
		// store all <label, labelId> pairs
		Map<String, Integer> globalLabelToIdMap = Maps.newHashMap();
		// store all <labelId, label> pairs
		Map<Integer, String> globalIdToLabelMap = Maps.newHashMap();
		
		// generate label id
		for(String label : context.getVectorMetadata().labels()) {
			Integer labelId = globalLabelToIdMap.get(label);
			if(labelId == null) {
				++labelNumber;
				labelId = labelNumber;
				globalLabelToIdMap.put(label, labelId);
				globalIdToLabelMap.put(labelId, label);
			}
		}
		
		// generate word id from featured term collection
		for(TermFeatureable term : context.getVectorMetadata().featuredTerms()) {
			term.setId(++wordNumber);
		}
		
		// store meta data
		context.getVectorMetadata().putLabelToIdPairs(globalLabelToIdMap);
		context.getVectorMetadata().putIdToLabelPairs(globalIdToLabelMap);
		
		// output featured term vector
		outputFeatureTermVector();
	}
	
	private void outputFeatureTermVector() {
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(context.getFDMetadata().getFeatureTermVectorFile()), context.getCharset()));
			for(TermFeatureable term : context.getVectorMetadata().featuredTerms()) {
				String word = term.getWord();
				Integer wordId = term.getId();
				StringBuffer buf = new StringBuffer();
				buf
					.append(word).append("\t")
					.append(wordId);
				LOG.debug("Write feature term vector: word=" + word + ", datum=" + buf.toString());
				w.write(buf.toString());
				w.newLine();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeQuietly(w);
		}
	}

}
