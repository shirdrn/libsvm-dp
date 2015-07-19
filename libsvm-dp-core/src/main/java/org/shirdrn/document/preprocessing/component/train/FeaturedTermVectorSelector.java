package org.shirdrn.document.preprocessing.component.train;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.FeatureTermSelector;
import org.shirdrn.document.preprocessing.api.TermFeatureable;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.common.AbstractComponent;
import org.shirdrn.document.preprocessing.utils.ReflectionUtils;

/**
 * Select term vector from the procedure of processing train data. The selection can
 * be based on any effective metric, here we use the Chi-square distance metric to
 * choose suitable terms. Certainly you can use another one to replace the
 * default one.</br>
 * If you have a huge data set which can fit in memory, the better practices
 * are plus a cache for caching computation result, because there are too many
 * identical computation logic.
 * 
 * @author Shirdrn
 */
public class FeaturedTermVectorSelector extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(FeaturedTermVectorSelector.class);
	private final FeatureTermSelector featuredTermsSelector;
	
	public FeaturedTermVectorSelector(final Context context) {
		super(context);
		String selectorClazz = context.getConfiguration().get(
				ConfigKeys.FEATURE_VECTOR_SELECTOR_CLASS, 
				"org.shirdrn.document.processor.feature.ChiFeaturedTermsSelector");
		LOG.info("Feature term vector selector: selectorClazz=" + selectorClazz);
		featuredTermsSelector = ReflectionUtils.newInstance(selectorClazz, FeatureTermSelector.class);
	}

	@Override
	public void fire() {
		// select feature term vector 
		Set<TermFeatureable> featureTerms = featuredTermsSelector.select(context);
		context.getVectorMetadata().setFeaturedTerms(featureTerms);
	}
	
}
