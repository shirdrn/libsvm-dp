package org.shirdrn.document.preprocessing.common;

public interface Component {

	void fire();
	Component getNext();
	Component setNext(Component next);
}
