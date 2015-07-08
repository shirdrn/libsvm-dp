package org.shirdrn.document.preprocessing.api;

import java.util.Map;

public interface TermFilter {

	void filter(Map<String, Term> terms);
}
