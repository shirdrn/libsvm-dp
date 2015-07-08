package org.shirdrn.document.preprocessing.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface VectorMetadata {
	
	void addLabel(String label);
	
	List<String> getLabels();
	
	int getTotalDocCount();
	
	void setTotalDocCount(int totalDocCount);
	
	int getLabelledTotalDocCount(String label);
	
	void putLabelledTotalDocCount(String label, int labelledDocCount);
	
	
	//////// inverted table ////////
	
	int getDocCount(Term term, String label);
	
	void addTermToInvertedTable(String label, String doc, Term term);
	
	void addTermsToInvertedTable(String label, String doc, Map<String, Term> terms);
	
	int getDocCount(Term term);
	
	Iterator<Entry<String, Map<String, Set<String>>>> invertedTableIterator();
	
	int getDocCountInThisLabel(Term term);
	
	//////// term table ////////
	
	int getDocCount(String label);
	
	int getTermCount(String label, String doc);
	
	void addTerms(String label, String doc, Map<String, Term> terms);
	
	int getLabelCount();
	
	Iterator<Entry<String, Map<String, Map<String, Term>>>> termTableIterator();
	
	//////// label vector map ////////
	
	// label->id
	
	Iterator<Entry<String, Integer>> labelVectorMapIterator();
	
	Integer getlabelId(String label);
	
	void putLabelToIdPairs(Map<String, Integer> globalLabelToIdMap);
	
	// id->label
	
	void putIdToLabelPairs(Map<Integer, String> globalIdToLabelMap);
	
	String getlabel(Integer labelId);
	
	Set<String> getWordSet();
	
	Integer getFeaturedTermId(String word);
	
	//////// featured term vector map ////////
	
	boolean containsFeaturedWord(String word);
	
	Iterator<Entry<String,Term>> featuredTermVectorIterator();
	
	void collectFeaturedTerm(String label, String word, Term term);
	
	void addToMergeFeaturedTerm(String word, Term term);
	
	Iterator<Entry<String, Map<String, Term>>> labelToFeaturedTermVectorsIterator();
}
