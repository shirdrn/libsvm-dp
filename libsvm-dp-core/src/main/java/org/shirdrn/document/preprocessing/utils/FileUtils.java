package org.shirdrn.document.preprocessing.utils;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtils {

	private static final Log LOG = LogFactory.getLog(FileUtils.class);
	
	public static void closeQuietly(Closeable... streams) {
		for(Closeable stream : streams) {
			try {
				stream.close();
			} catch (Exception e) {
				LOG.warn("Fail to close: ", e);
			}
		}
	}
	
	public static void close(Closeable... streams) throws IOException {
		for(Closeable stream : streams) {
			stream.close();
		}
	}
}
