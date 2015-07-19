package org.shirdrn.document.preprocessing.api;

import java.util.Set;

/**
 * It's used to choose term vector according to
 * a specified selection policy.
 * 
 * @author yanjun
 */
public interface FeatureTermSelector {

	Set<TermFeatureable> select(Context context);
	
	void load(Context context);
}
