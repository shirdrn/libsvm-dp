package org.shirdrn.document.preprocessing.api;

import java.util.Set;

/**
 * It's used to choose term vector from the train data set 
 * according to the specified selection policy.
 * 
 * @author yanjun
 */
public interface FeatureTermSelector {

	Set<TermFeatureable> select(Context context);
	
	void load(Context context);
}
