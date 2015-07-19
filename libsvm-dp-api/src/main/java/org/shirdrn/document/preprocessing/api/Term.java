package org.shirdrn.document.preprocessing.api;

/**
 * A term contains related information about a word.
 * 
 * @author yanjun
 */
public interface Term extends TermReadable {

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
	
}
