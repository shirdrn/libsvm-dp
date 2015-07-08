package org.shirdrn.document.preprocessing.api;

import java.io.File;
import java.util.Map;

public interface DocumentAnalyzer {

	Map<String, Term> analyze(File file);
}
