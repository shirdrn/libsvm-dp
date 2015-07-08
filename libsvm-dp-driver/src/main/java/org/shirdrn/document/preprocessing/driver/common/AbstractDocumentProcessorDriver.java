package org.shirdrn.document.preprocessing.driver.common;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.common.Component;
import org.shirdrn.document.preprocessing.common.ContextImpl;
import org.shirdrn.document.preprocessing.utils.ReflectionUtils;

public abstract class AbstractDocumentProcessorDriver {

	public abstract void process();
	
	protected void run(Component[] chain) {
		for (int i = 0; i < chain.length - 1; i++) {
			Component current = chain[i];
			Component next = chain[i + 1];
			current.setNext(next);
		}
		
		for (Component componennt : chain) {
			componennt.fire();
		}
	}
	
	public Context newContext(ProcessorType type, String config) {
		return new ContextImpl(type, config);
	}
	
	public static void start(Class<? extends AbstractDocumentProcessorDriver> driverClass) {
		AbstractDocumentProcessorDriver driver = ReflectionUtils.newInstance(driverClass);
		driver.process();
	}
}
