package org.shirdrn.document.preprocessing.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.VectorMetadata;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class VectorMetadataImpl implements VectorMetadata {

	private int totalDocCount;
	private final List<String> labels = new ArrayList<String>();
	// Map<类别, 文档数量>
	private final Map<String, Integer> labelledTotalDocCountMap = Maps.newHashMap();
	//  Map<类别, Map<文档 ,Map<词, 词信息>>>
	private final Map<String, Map<String, Map<String, Term>>> termTable = Maps.newHashMap();
	//  Map<词 ,Map<类别, Set<文档>>>
	private final Map<String, Map<String, Set<String>>> invertedTable = Maps.newHashMap();
	
	// <labelId, label>
	private final Map<Integer, String> globalIdToLabelMap = Maps.newHashMap();
	// <label, labelId>
	private final Map<String, Integer> globalLabelToIdMap = Maps.newHashMap();
	
	// Map<label, Map<word, term>>
	private final Map<String, Map<String, Term>> labelToFeaturedTermVectorsMap = Maps.newHashMap();
	// Map<word, term>, finally merged vector
	private final Map<String, Term> mergedFeaturedTermVectorMap = Maps.newHashMap();
	
	@Override
	public void addLabel(String label) {
		if(!labels.contains(label)) {
			labels.add(label);
		}
	}
	
	@Override
	public List<String> getLabels() {
		return labels;
	}
	
	@Override
	public int getTotalDocCount() {
		return totalDocCount;
	}
	
	@Override
	public void setTotalDocCount(int totalDocCount) {
		this.totalDocCount = totalDocCount;
	}
	
	@Override
	public int getLabelledTotalDocCount(String label) {
		return labelledTotalDocCountMap.get(label);
	}
	
	@Override
	public void putLabelledTotalDocCount(String label, int labelledDocCount) {
		labelledTotalDocCountMap.put(label, labelledDocCount);
	}
	
	
	//////// inverted table ////////
	
	@Override
	public int getDocCount(Term term, String label) {
		String word = term.getWord();
		return invertedTable.get(word).get(label).size();
	}
	
	@Override
	public synchronized void addTermToInvertedTable(String label, String doc, Term term) {
		String word = term.getWord();
		Map<String, Set<String>> labelledDocs = invertedTable.get(word);
		if(labelledDocs == null) {
			labelledDocs = Maps.newHashMap();
			invertedTable.put(word, labelledDocs);
		}
		Set<String> docs = labelledDocs.get(label);
		if(docs == null) {
			docs = Sets.newHashSet();
			labelledDocs.put(label, docs);
		}
		docs.add(doc);
	}
	
	@Override
	public void addTermsToInvertedTable(String label, String doc, Map<String, Term> terms) {
		Iterator<Entry<String, Term>> iter = terms.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Term> entry = iter.next();
			addTermToInvertedTable(label, doc, entry.getValue());
		}
	}
	
	@Override
	public int getDocCount(Term term) {
		String word = term.getWord();
		int count = 0;
		Map<String, Set<String>> labelledDocs = invertedTable.get(word);
		Iterator<Entry<String, Set<String>>> iter = labelledDocs.entrySet().iterator();
		while(iter.hasNext()) {
			count += iter.next().getValue().size();
		}
		return count;
	}
	
	@Override
	public Iterator<Entry<String, Map<String, Set<String>>>> invertedTableIterator() {
		return invertedTable.entrySet().iterator();
	}
	
	@Override
	public int getDocCountInThisLabel(Term term) {
		return invertedTable.get(term.getWord()).size();
	}
	
	//////// term table ////////
	
	@Override
	public int getDocCount(String label) {
		return termTable.get(label).size();
	}
	
	@Override
	public int getTermCount(String label, String doc) {
		int size = 0;
		// avoid empty file
		if(termTable.get(label) != null && termTable.get(label).get(doc) != null) {
			size = termTable.get(label).get(doc).size();
		}
		return size;
	}
	
	@Override
	public synchronized void addTerms(String label, String doc, Map<String, Term> terms) {
		Map<String, Map<String, Term>> docs = termTable.get(label);
		if(docs == null) {
			docs = Maps.newHashMap();
			termTable.put(label, docs);
		}
		docs.put(doc, terms);
	}
	
	@Override
	public int getLabelCount() {
		return termTable.keySet().size();
	}
	
	@Override
	public Iterator<Entry<String, Map<String, Map<String, Term>>>> termTableIterator() {
		return termTable.entrySet().iterator();
	}
	
	//////// label vector map ////////
	
	// label->id
	@Override
	public Iterator<Entry<String, Integer>> labelVectorMapIterator() {
		return globalLabelToIdMap.entrySet().iterator();
	}
	
	@Override
	public Integer getlabelId(String label) {
		return globalLabelToIdMap.get(label);
	}
	
	@Override
	public void putLabelToIdPairs(Map<String, Integer> globalLabelToIdMap) {
		this.globalLabelToIdMap.putAll(globalLabelToIdMap);
	}
	
	// id->label
	@Override
	public void putIdToLabelPairs(Map<Integer, String> globalIdToLabelMap) {
		this.globalIdToLabelMap.putAll(globalIdToLabelMap);
	}
	
	@Override
	public String getlabel(Integer labelId) {
		return globalIdToLabelMap.get(labelId);
	}
	
	@Override
	public Set<String> getWordSet() {
		return invertedTable.keySet();
	}
	
	//////// featured vectors ////////
	
	@Override
	public Iterator<Entry<String, Map<String, Term>>> labelToFeaturedTermVectorsIterator() {
		return labelToFeaturedTermVectorsMap.entrySet().iterator();
	}
	
	@Override
	public Integer getFeaturedTermId(String word) {
		Term term = mergedFeaturedTermVectorMap.get(word);
		if(term != null) {
			return term.getId();
		}
		return null;
	}

	@Override
	public boolean containsFeaturedWord(String word) {
		return mergedFeaturedTermVectorMap.containsKey(word);
	}

	@Override
	public Iterator<Entry<String, Term>> featuredTermVectorIterator() {
		return mergedFeaturedTermVectorMap.entrySet().iterator();
	}

	@Override
	public void collectFeaturedTerm(String label, String word, Term term) {
		Map<String,Term> words = labelToFeaturedTermVectorsMap.get(label);
		if(words == null) {
			words = Maps.newHashMap();
			labelToFeaturedTermVectorsMap.put(label, words);
		}
		words.put(word, term);		
	}

	@Override
	public void addToMergeFeaturedTerm(String word, Term term) {
		if(!mergedFeaturedTermVectorMap.containsKey(word)) {
			mergedFeaturedTermVectorMap.put(word, term);
		}		
	}
	
}
