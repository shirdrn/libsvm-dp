package org.shirdrn.document.preprocessing.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface VectorMetadata {
	
	void addLabel(String label);
	
	List<String> labels();
	
	int totalDocCount();
	
	void setTotalDocCount(int totalDocCount);
	
	void putLabelledTotalDocCount(String label, int labelledDocCount);
	
	
	//////// inverted table ////////
	
	void addTermToInvertedTable(String label, String doc, Term term);
	
	void addTermsToInvertedTable(String label, String doc, Map<String, Term> terms);
	
	int docCount(Term term);
	
	Iterator<Entry<String, Map<String, Set<String>>>> invertedTableIterator();
	
	//////// term table ////////
	
	int termCount(String label, String doc);
	
	void addTerms(String label, String doc, Map<String, Term> terms);
	
	int labelCount();
	
	Iterator<Entry<String, Map<String, Map<String, Term>>>> termTableIterator();
	
	//////// label vector map ////////
	
	// label->id
	
	Iterator<Entry<String, Integer>> labelVectorMapIterator();
	
	Integer getlabelId(String label);
	
	void putLabelToIdPairs(Map<String, Integer> globalLabelToIdMap);
	
	// id->label
	
	void putIdToLabelPairs(Map<Integer, String> globalIdToLabelMap);
	
	String getLabelById(Integer labelId);
	
	//////// featured terms vector ////////
	
	void setFeaturedTerms(Set<TermFeatureable> terms);
	Set<TermFeatureable> featuredTerms();
}
