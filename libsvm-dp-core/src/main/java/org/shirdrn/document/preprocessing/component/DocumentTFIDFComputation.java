package org.shirdrn.document.preprocessing.component;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.common.AbstractComponent;
import org.shirdrn.document.preprocessing.common.FeaturedTerm;
import org.shirdrn.document.preprocessing.utils.MetricUtils;

public class DocumentTFIDFComputation extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(DocumentTFIDFComputation.class);
	private Set<TermFeatureable> featuredTerms;
	
	public DocumentTFIDFComputation(final Context context) {
		super(context);
	}

	@Override
	public void fire() {
		featuredTerms = context.getVectorMetadata().featuredTerms();
		
		// for each document, compute TF, IDF, TF-IDF
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> labelledDocsEntry = iter.next();
			String label = labelledDocsEntry.getKey();
			LOG.info("Compute TF-IDF for:label=" + label);
			Map<String, Map<String, Term>>  docs = labelledDocsEntry.getValue();
			Iterator<Entry<String, Map<String, Term>>> docsIter = docs.entrySet().iterator();
			while(docsIter.hasNext()) {
				Entry<String, Map<String, Term>> docsEntry = docsIter.next();
				String doc = docsEntry.getKey();
				Map<String, Term> terms = docsEntry.getValue();
				Iterator<Entry<String, Term>> termsIter = terms.entrySet().iterator();
				LOG.debug("label=" + label + ", doc=" + doc + ", terms=" + terms);
				while(termsIter.hasNext()) {
					Entry<String, Term> termEntry = termsIter.next();
					String word = termEntry.getKey();
					// check whether word is a featured word
					if(isFeaturedWord(word)) {
						Term term = termEntry.getValue();
						int freq = term.getFreq();
						int termCount = context.getVectorMetadata().termCount(label, doc);
						double tf = MetricUtils.tf(freq, termCount);
						int totalDocCount = context.getVectorMetadata().totalDocCount();
						int docCountContainingTerm = context.getVectorMetadata().docCount(term);
						
						double idf = MetricUtils.idf(totalDocCount, docCountContainingTerm);
						termEntry.getValue().setIdf(idf);
						termEntry.getValue().setTf(tf);
						termEntry.getValue().setTfidf(MetricUtils.tfidf(tf, idf));
						LOG.debug("Term detail: label=" + label + ", doc=" + doc + ", term=" + term);
					} else {
						// remove term not contained in feature vector
						termsIter.remove();
						LOG.debug("Not in CHI vector: word=" + word);
					}
				}
			}
			LOG.info("TF-IDF computed: label=" + label);
		}		
	}
	
	private boolean isFeaturedWord(String word) {
		return featuredTerms.contains(new FeaturedTerm(word));
	}
	
}
