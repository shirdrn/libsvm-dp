package org.shirdrn.document.preprocessing.measure.chi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.FeatureTermSelector;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.common.FeaturedTerm;
import org.shirdrn.document.preprocessing.measure.utils.MergeTermsPolicies.SimpleMergeTermsPolicy;
import org.shirdrn.document.preprocessing.measure.utils.SortUtils;
import org.shirdrn.document.preprocessing.measure.utils.SortUtils.Result;
import org.shirdrn.document.preprocessing.utils.FileUtils;
import org.shirdrn.document.preprocessing.utils.NamedThreadFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ChiFeatureTermSelector implements FeatureTermSelector {

	private static final Log LOG = LogFactory.getLog(ChiFeatureTermSelector.class);
	private Context context;
	
	// Map<label, Map<word, term>>
	private final Map<String, Map<String, TermFeatureable>> labeledTermsWithMeasures = Maps.newHashMap();
	// Map<label, ChiCalculator>
	private final Map<String, ChiCalculator> calculators = Maps.newHashMap();
	
	private ExecutorService executorService;
	private CountDownLatch latch;
	private MergeTermsPolicy mergeTermsPolicy;
	private float keptTermsPercent;
	
	public ChiFeatureTermSelector() {
		super();
	}
	
	@Override
	public Set<TermFeatureable> select(Context context) {
		this.context = context;
		this.mergeTermsPolicy = new SimpleMergeTermsPolicy(this);
		keptTermsPercent = context.getConfiguration().getFloat(ConfigKeys.FEATURE_EACH_LABEL_KEPT_TERM_PERCENT, 0.30F);
		
		int labelCnt = context.getVectorMetadata().labelCount();
		LOG.info("Initialize latch: labelCnt=" + labelCnt);
		latch = new CountDownLatch(labelCnt);
		executorService = Executors.newCachedThreadPool(new NamedThreadFactory("CHI"));
		try {
			for(String label : context.getVectorMetadata().labels()) {
				labeledTermsWithMeasures.put(label, Maps.<String,TermFeatureable>newHashMap());
				calculators.put(label, new ChiCalculator(label, labeledTermsWithMeasures.get(label)));
				executorService.execute(calculators.get(label));
			}
		} finally {
			try {
				latch.await();
			} catch (InterruptedException e) { }
			LOG.info("Shutdown executor service: " + executorService);
			executorService.shutdown();
		}
		
		stat();
		
		// merge CHI terms for all labels
		return mergeTermsPolicy.merge(context, labeledTermsWithMeasures);
	}
	
	private void stat() {
		Iterator<Entry<String, Map<String, TermFeatureable>>> iter = labeledTermsWithMeasures.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, TermFeatureable>> entry = iter.next();
			String label = entry.getKey();
			LOG.info("STAT: label=" + label + ", featuredTerms=" + entry.getValue().size());
		}
	}

	private final class ChiCalculator extends Thread {
		
		private final String label;
		private final Map<String, TermFeatureable> terms;
		private Result sortedResult;
		
		public ChiCalculator(String label, Map<String, TermFeatureable> terms) {
			this.label = label;
			this.terms = terms;
		}
		
		@Override
		public void run() {
			try {
				LOG.info("Calc CHI: label=" + label);
				processSingleLabelledData();
				LOG.info("CHI calculated: label=" + label + ", termsCount=" + terms.size());
				
				LOG.info("Sort CHI terms for: label=" + label);
				Date start = new Date();
				int topN = (int) (terms.size() * keptTermsPercent);
				LOG.info("Terms selection for sort: topN=" + topN);
				sortedResult = sort(terms, topN);
				Date finish = new Date();
				LOG.info("CHI terms sorted: label=" + label + ", timeTaken=" + (finish.getTime() - start.getTime()) + "(ms)");
			} finally {
				latch.countDown();
			}
		}
		
		private Result sort(Map<String, TermFeatureable> terms, int topN) {
			SortUtils sorter = new SortUtils(terms, true, Math.min(topN, terms.size()));
			Result result = sorter.heapSort();
			return result;
		}
		
		private void processSingleLabelledData() {
			Iterator<Entry<String, Map<String, Set<String>>>> iter = context.getVectorMetadata().invertedTableIterator();
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
				Iterator<Entry<String, Set<String>>> labelledIter = labelledDocs.entrySet().iterator();
				while(labelledIter.hasNext()) {
					Entry<String, Set<String>> labelledEntry = labelledIter.next();
					String tmpLabel = labelledEntry.getKey();
					if(!label.equals(tmpLabel)) {
						docCountContainingWordNotInLabel += labelledEntry.getValue().size();
					}
				}
				
				// C: doc count not containing the word in this label
				int docCountNotContainingWordInLabel = computeDocCountNotContainingWordInLabel(word, label);
				
				// D: doc count not containing the word not in this label
				int docCountNotContainingWordNotInLabel = computeDocCountNotContainingWordNotInLabel(word, label);
				
				// compute CHI value
				int N = context.getVectorMetadata().totalDocCount();
				int A = docCountContainingWordInLabel;
				int B = docCountContainingWordNotInLabel;
				int C = docCountNotContainingWordInLabel;
				int D = docCountNotContainingWordNotInLabel;
				int temp = (A*D-B*C);
				double chi = (double) N*temp*temp / ((A+C)*(A+B)*(B+D)*(C+D));
				TermFeatureable term = new FeaturedTerm(word);
				term.setMeasureValue(chi);
				terms.put(word, term);
			}
		}

		private int computeDocCountNotContainingWordInLabel(String word, String label) {
			int count = 0;
			Iterator<Entry<String,Map<String,Map<String,Term>>>> iter = context.getVectorMetadata().termTableIterator();
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
		
		private int computeDocCountNotContainingWordNotInLabel(String word, String label) {
			int count = 0;
			Iterator<Entry<String,Map<String,Map<String,Term>>>> iter = context.getVectorMetadata().termTableIterator();
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
	}
	
	public Result getSortedResult(String label) {
		return calculators.get(label).sortedResult;
	}
	
	@Override
	public void load(Context context) {
		BufferedReader reader = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(context.getFDMetadata().getFeatureTermVectorFile());
			reader = new BufferedReader(new InputStreamReader(fis, context.getCharset()));
			String line = null;
			Set<TermFeatureable> terms = Sets.newHashSet();
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(!line.isEmpty()) {
					String[] aWord = line.split("\\s+");
					if(aWord.length == 2) {
						String word = aWord[0];
						int wordId = Integer.parseInt(aWord[1]);
						FeaturedTerm term = new FeaturedTerm(word);
						term.setId(wordId);
						terms.add(term);
						LOG.info("Load CHI term: word=" + word + ", wordId=" + wordId);
					}
				}
			}
			context.getVectorMetadata().setFeaturedTerms(terms);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.closeQuietly(fis, reader);
		}		
	}

}
