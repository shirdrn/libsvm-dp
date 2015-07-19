package org.shirdrn.document.preprocessing.common;

import org.shirdrn.document.preprocessing.api.Term;

public class TermImpl extends AbstractTerm implements Term {

	private int id;
	private String lexicalCategory = "unknown";
	private int freq = 0;
	private double tf;
	private double idf;
	private double tfidf = 0;
	private double measureValue = 0;
	
	public TermImpl(String word) {
		super(word);
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getLexicalCategory() {
		return lexicalCategory;
	}

	public void setLexicalCategory(String lexicalCategory) {
		this.lexicalCategory = lexicalCategory;
	}

	public int getFreq() {
		return freq;
	}

	public void incrFreq() {
		freq++;
	}

	public double getTf() {
		return tf;
	}

	public void setTf(double tf) {
		this.tf = tf;
	}

	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}

	public double getTfidf() {
		return tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}

	@Override
	public int hashCode() {
		return word.hashCode();
	}
	
	public double getMeasureValue() {
		return measureValue;
	}

	public void setMeasureValue(double measureValue) {
		this.measureValue = measureValue;
	}

	@Override
	public boolean equals(Object obj) {
		TermImpl other = (TermImpl) obj;
		return word.equals(other.word);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[")
			.append("word=").append(word).append(", ")
			.append("freq=").append(freq).append(", ")
			.append("tf=").append(tf).append(", ")
			.append("idf=").append(idf).append(", ")
			.append("tf-idf=").append(tfidf).append(", ")
			.append("]");
		return buffer.toString();
	}
}
