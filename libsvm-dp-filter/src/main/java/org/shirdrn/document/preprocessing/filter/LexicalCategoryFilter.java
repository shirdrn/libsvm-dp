package org.shirdrn.document.preprocessing.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFilter;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;

public class LexicalCategoryFilter implements TermFilter {

	private final Set<String> keptLexicalCategories = new HashSet<String>();
	
	public LexicalCategoryFilter(Context context) {
		// read configured lexical categories
		String lexicalCategories = 
				context.getConfiguration().get(ConfigKeys.DOCUMENT_FILTER_KEPT_LEXICAL_CATEGORIES, "n");
		for(String category : lexicalCategories.split("\\s*,\\s*")) {
			keptLexicalCategories.add(category);
		}
	}
	
	@Override
	public void filter(Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			if(!keptLexicalCategories.contains(entry.getValue().getLexicalCategory())) {
				iter.remove();
			}
		}
	}

}
