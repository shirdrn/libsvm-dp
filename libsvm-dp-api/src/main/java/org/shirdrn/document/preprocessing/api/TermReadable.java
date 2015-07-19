package org.shirdrn.document.preprocessing.api;

public interface TermReadable {

	void setId(Integer id);
	Integer getId();
	
	String getWord();
	void setWord(String word);
}
