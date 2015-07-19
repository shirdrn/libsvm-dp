package org.shirdrn.document.preprocessing.api;

public interface TermFeatureable extends TermReadable {

	double getMeasureValue();

	void setMeasureValue(double measureValue);
	
}
