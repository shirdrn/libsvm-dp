package org.shirdrn.document.preprocessing.utils;

import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.ProcessorType;
import org.shirdrn.document.preprocessing.api.VectorMetadata;
import org.shirdrn.document.preprocessing.common.Component;
import org.shirdrn.document.preprocessing.common.ContextImpl;
import org.shirdrn.document.preprocessing.common.VectorMetadataImpl;

public class PreprocessingUtils {

	public static VectorMetadata newVectorMetadata() {
		return new VectorMetadataImpl();
	}
	
	public static Context newContext(ProcessorType type, String config) {
		return new ContextImpl(type, config);
	}
	
	public static Component[] newChainedComponents(final Context context, Class<?>[] classes) {
		final int nComponent = classes.length;
		Component[] components = new Component[nComponent];
		for(int i=0; i<classes.length; i++) {
			components[i] = ReflectionUtils.newInstance(classes[i], Component.class, new Object[] {context});
		}
		return components;
	}
}
