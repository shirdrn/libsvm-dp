package org.shirdrn.document.preprocessing.api;

public interface Term {

	int getId();

	void setId(int id);

	String getWord();

	void setWord(String word);

	String getLexicalCategory();

	void setLexicalCategory(String lexicalCategory);

	int getFreq();

	void incrFreq();

	double getTf();

	void setTf(double tf);

	double getIdf();

	void setIdf(double idf);

	double getTfidf();

	void setTfidf(double tfidf);


	double getMeasureValue();

	void setMeasureValue(double measureValue);
}
