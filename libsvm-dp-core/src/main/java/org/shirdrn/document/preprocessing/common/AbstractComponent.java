package org.shirdrn.document.preprocessing.common;

import org.shirdrn.document.preprocessing.api.Context;

public abstract class AbstractComponent implements Component {

	protected final Context context;
	private Component next;
	
	public AbstractComponent(Context context) {
		this.context = context;
	}
	
	@Override
	public Component getNext() {
		return next;
	}
	
	@Override
	public Component setNext(Component next) {
		this.next = next;	
		return next;
	}
	
}
