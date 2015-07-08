package org.shirdrn.document.preprocessing.measure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.FeaturedTermsSelector;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.common.TermImpl;
import org.shirdrn.document.preprocessing.measure.utils.SortUtils;
import org.shirdrn.document.preprocessing.measure.utils.SortUtils.Result;

public class ChiFeaturedTermsSelector implements FeaturedTermsSelector {

	private static final Log LOG = LogFactory.getLog(ChiFeaturedTermsSelector.class);
	private Context context;
	private int keptTermCountEachLabel;
	
	@Override
	public void select(Context context) {
		this.context = context;
		keptTermCountEachLabel = context.getConfiguration().getInt(ConfigKeys.FEATURE_EACH_LABEL_KEPT_TERM_COUNT, 3000);
		// compute CHI value for selecting feature terms 
		// after sorting by CHI value
		for(String label : context.getVectorMetadata().getLabels()) {
			// for each label, compute CHI vector
			LOG.info("Compute CHI for: label=" + label);
			processOneLabel(label);
		}
		
		// sort and select CHI vectors
		Iterator<Entry<String, Map<String, Term>>> chiIter = 
				context.getVectorMetadata().labelToFeaturedTermVectorsIterator();
		while(chiIter.hasNext()) {
			Entry<String, Map<String, Term>> entry = chiIter.next();
			String label = entry.getKey();
			LOG.info("Sort CHI terms for: label=" + label + ", termCount=" + entry.getValue().size());
			Result result = sort(entry.getValue());
			for (int i = result.getStartIndex(); i <= result.getEndIndex(); i++) {
				Entry<String, Term> termEntry = result.get(i);
				// merge CHI terms for all labels
				context.getVectorMetadata().addToMergeFeaturedTerm(termEntry.getKey(), termEntry.getValue());
			}
		}
	}
	
	private Result sort(Map<String, Term> terms) {
		SortUtils sorter = new SortUtils(terms, true, keptTermCountEachLabel);
		Result result = sorter.heapSort();
		return result;
	}

	private void processOneLabel(String label) {
		Iterator<Entry<String, Map<String, Set<String>>>> iter = 
				context.getVectorMetadata().invertedTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Set<String>>> entry = iter.next();
			String word = entry.getKey();
			Map<String, Set<String>> labelledDocs = entry.getValue();
			
			// A: doc count containing the word in this label
			int docCountContainingWordInLabel = 0;
			if(labelledDocs.get(label) != null) {
				docCountContainingWordInLabel = labelledDocs.get(label).size();
			}
			
			// B: doc count containing the word not in this label
			int docCountContainingWordNotInLabel = 0;
			Iterator<Entry<String, Set<String>>> labelledIter = 
					labelledDocs.entrySet().iterator();
			while(labelledIter.hasNext()) {
				Entry<String, Set<String>> labelledEntry = labelledIter.next();
				String tmpLabel = labelledEntry.getKey();
				if(!label.equals(tmpLabel)) {
					docCountContainingWordNotInLabel += labelledEntry.getValue().size();
				}
			}
			
			// C: doc count not containing the word in this label
			int docCountNotContainingWordInLabel = 
					getDocCountNotContainingWordInLabel(word, label);
			
			// D: doc count not containing the word not in this label
			int docCountNotContainingWordNotInLabel = 
					getDocCountNotContainingWordNotInLabel(word, label);
			
			// compute CHI value
			int N = context.getVectorMetadata().getTotalDocCount();
			int A = docCountContainingWordInLabel;
			int B = docCountContainingWordNotInLabel;
			int C = docCountNotContainingWordInLabel;
			int D = docCountNotContainingWordNotInLabel;
			int temp = (A*D-B*C);
			// double chi = (double) N*temp*temp / (A+C)*(A+B)*(B+D)*(C+D); // incorrect!!!
			double chi = (double) N*temp*temp / ((A+C)*(A+B)*(B+D)*(C+D)); // correct formula computation
			Term term = new TermImpl(word);
			term.setMeasureValue(chi);
			context.getVectorMetadata().collectFeaturedTerm(label, word, term);
		}
	}

	private int getDocCountNotContainingWordInLabel(String word, String label) {
		int count = 0;
		Iterator<Entry<String,Map<String,Map<String,Term>>>> iter = 
				context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String,Map<String,Map<String,Term>>> entry = iter.next();
			String tmpLabel = entry.getKey();
			// in this label
			if(tmpLabel.equals(label)) {
				Map<String, Map<String, Term>> labelledDocs = entry.getValue();
				for(Entry<String, Map<String, Term>> docEntry : labelledDocs.entrySet()) {
					// not containing this word
					if(!docEntry.getValue().containsKey(word)) {
						++count;
					}
				}
				break;
			}
		}
		return count;
	}
	
	private int getDocCountNotContainingWordNotInLabel(String word, String label) {
		int count = 0;
		Iterator<Entry<String,Map<String,Map<String,Term>>>> iter = 
				context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String,Map<String,Map<String,Term>>> entry = iter.next();
			String tmpLabel = entry.getKey();
			// not in this label
			if(!tmpLabel.equals(label)) {
				Map<String, Map<String, Term>> labelledDocs = entry.getValue();
				for(Entry<String, Map<String, Term>> docEntry : labelledDocs.entrySet()) {
					// not containing this word
					if(!docEntry.getValue().containsKey(word)) {
						++count;
					}
				}
			}
		}
		return count;
	}

	@Override
	public void load(Context context) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(
					context.getFDMetadata().getFeatureTermVectorFile()), context.getCharset()));
			String line = null;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.isEmpty()) {
					String[] aWord = line.split("\\s+");
					if(aWord.length == 2) {
						String word = aWord[0];
						int wordId = Integer.parseInt(aWord[1]);
						TermImpl term = new TermImpl(word);
						term.setId(wordId);
						LOG.info("Load CHI term: word=" + word + ", wordId=" + wordId);
						context.getVectorMetadata().addToMergeFeaturedTerm(word, term);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}

}
