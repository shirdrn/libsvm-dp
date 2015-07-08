package org.shirdrn.document.preprocessing.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFilter;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.utils.ReflectionUtils;

public class AggregatedTermFilter implements TermFilter {

	private final List<TermFilter> filters = new ArrayList<TermFilter>(0);
	
	public AggregatedTermFilter(Context context) {
		String classes = context.getConfiguration().get(ConfigKeys.DOCUMENT_ANALYZER_CLASS);
		if(classes != null) {
			String[] aClass = classes.split("[,;\\s\\|:-]+");
			for(String className : aClass) {
				filters.add(ReflectionUtils.newInstance(className, TermFilter.class));
			}
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

}
