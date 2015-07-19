package org.shirdrn.document.preprocessing.measure.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.measure.chi.ChiFeatureTermSelector;
import org.shirdrn.document.preprocessing.measure.chi.MergeTermsPolicy;
import org.shirdrn.document.preprocessing.measure.utils.SortUtils.Result;

import com.google.common.collect.Sets;

public class MergeTermsPolicies {

	public static final class SimpleMergeTermsPolicy implements MergeTermsPolicy {

		private static final Log LOG = LogFactory.getLog(SimpleMergeTermsPolicy.class);
		private final ChiFeatureTermSelector selector;
		
		public SimpleMergeTermsPolicy(ChiFeatureTermSelector selector) {
			this.selector = selector;
		}
		
		@Override
		public Set<TermFeatureable> merge(Context context, Map<String, Map<String, TermFeatureable>> terms) {
			// merge CHI vectors
			Set<TermFeatureable> mergedTerms = Sets.newHashSet();
			Iterator<Entry<String, Map<String, TermFeatureable>>> chiIter = terms.entrySet().iterator();
			while(chiIter.hasNext()) {
				Entry<String, Map<String, TermFeatureable>> entry = chiIter.next();
				String label = entry.getKey();
				Result sortedResult = selector.getSortedResult(label);
				LOG.info("Result: label=" + label + ", startIndex=" + sortedResult.getStartIndex() + ", endIndex=" + sortedResult.getEndIndex());
				StringBuffer sb = new StringBuffer();
				for (int i = sortedResult.getStartIndex(); i <= sortedResult.getEndIndex(); i++) {
					Entry<String, TermFeatureable> termEntry = sortedResult.get(i);
					// merge CHI terms for all labels
					mergedTerms.add(termEntry.getValue());
				}
				LOG.debug("label=" + label + ", terms=" + sb.toString().trim());
			}
			LOG.info("Merged terms: count=" + mergedTerms.size());
			return mergedTerms;
		}

	}
	
}
