package org.shirdrn.document.preprocessing.measure.chi;

import java.util.Map;
import java.util.Set;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.TermFeatureable;

public interface MergeTermsPolicy {

	Set<TermFeatureable> merge(Context context, Map<String, Map<String, TermFeatureable>> terms);
	
}
