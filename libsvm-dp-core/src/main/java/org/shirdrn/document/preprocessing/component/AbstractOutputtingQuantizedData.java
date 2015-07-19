package org.shirdrn.document.preprocessing.component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.common.AbstractComponent;
import org.shirdrn.document.preprocessing.component.test.OutputtingQuantizedTestData;
import org.shirdrn.document.preprocessing.utils.FileUtils;

import com.google.common.collect.Maps;

public abstract class AbstractOutputtingQuantizedData extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(AbstractOutputtingQuantizedData.class);
	private BufferedWriter writer;
	private Map<String, TermFeatureable> featuredTermsMap = Maps.newHashMap();
	
	public AbstractOutputtingQuantizedData(final Context context) {
		super(context);
	}
	
	@Override
	public void fire() {
		for(TermFeatureable term : context.getVectorMetadata().featuredTerms()) {
			featuredTermsMap.put(term.getWord(), term);
		}
		
		// create term vectors for outputting/inputting
		quantizeTermVectors();
		
		// output train/test vectors
		try {
			File file = new File(context.getFDMetadata().getOutputDir(), context.getFDMetadata().getOutputVectorFile());
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), context.getCharset()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> labelledDocsEntry = iter.next();
			String label = labelledDocsEntry.getKey();
			Integer labelId = getLabelId(label);
			if(labelId != null) {
				Map<String, Map<String, Term>>  docs = labelledDocsEntry.getValue();
				Iterator<Entry<String, Map<String, Term>>> docsIter = docs.entrySet().iterator();
				while(docsIter.hasNext()) {
					StringBuffer line = new StringBuffer();
					line.append(labelId).append(" ");
					Entry<String, Map<String, Term>> docsEntry = docsIter.next();
					Map<String, Term> terms = docsEntry.getValue();
					for(Entry<String, Term> termEntry : terms.entrySet()) {
						String word = termEntry.getKey();
						Integer wordId = getWordId(word);
						if(wordId != null) {
							Term term = termEntry.getValue();
							line.append(wordId).append(":").append(term.getTfidf()).append(" ");
						}
					}
					try {
						String element = line.toString().trim();
						LOG.debug("Write line: " + element);
						writer.write(element);
						writer.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				LOG.warn("Label ID can not be found: label=" + label + ", labelId=null");
			}
		}
		FileUtils.closeQuietly(writer);
		LOG.info("Finished: outputVectorFile=" + context.getFDMetadata().getOutputVectorFile());
	}
	
	private Integer getWordId(String word) {
		TermFeatureable term = featuredTermsMap.get(word);
		return term == null ? null : term.getId();
	}

	private Integer getLabelId(String label) {
		return context.getVectorMetadata().getlabelId(label);
	}

	protected abstract void quantizeTermVectors();

}
