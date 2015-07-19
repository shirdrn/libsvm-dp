package org.shirdrn.document.preprocessing.common;

import org.shirdrn.document.preprocessing.api.TermReadable;

public abstract class AbstractTerm implements TermReadable {
	
	protected Integer id;
	protected String word;
	
	public AbstractTerm(String word) {
		super();
		this.word = word;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getWord() {
		return word;
	}
	
	@Override
	public void setWord(String word) {
		this.word = word;		
	}
}
