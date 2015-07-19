package org.shirdrn.document.preprocessing.common;

import org.shirdrn.document.preprocessing.api.TermFeatureable;

public class FeaturedTerm extends AbstractTerm implements TermFeatureable {

	protected double measureValue;
	
	public FeaturedTerm(String word) {
		super(word);
	}

	@Override
	public double getMeasureValue() {
		return measureValue;
	}

	@Override
	public void setMeasureValue(double measureValue) {
		this.measureValue = measureValue;
	}
	
	@Override
	public int hashCode() {
		return 31 * word.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		FeaturedTerm other = (FeaturedTerm) obj;
		return this.word.equals(other.word);
	}
	
	@Override
	public String toString() {
		return "[word=" + word + ", value=" + Double.toString(measureValue) + "]";
	}

}
