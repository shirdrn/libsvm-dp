package org.shirdrn.document.preprocessing.component;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shirdrn.document.preprocessing.api.Context;
import org.shirdrn.document.preprocessing.api.DocumentAnalyzer;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.api.TermFilter;
import org.shirdrn.document.preprocessing.api.constants.ConfigKeys;
import org.shirdrn.document.preprocessing.common.AbstractComponent;
import org.shirdrn.document.preprocessing.utils.NamedThreadFactory;
import org.shirdrn.document.preprocessing.utils.ReflectionUtils;

public class DocumentWordsCollector extends AbstractComponent {
	
	private static final Log LOG = LogFactory.getLog(DocumentWordsCollector.class);
	private final Set<TermFilter> filters = new HashSet<TermFilter>();
	private ExecutorService executorService;
	private CountDownLatch latch;
	
	public DocumentWordsCollector(final Context context) {
		super(context);
		// load term filter classes
		String filterClassNames = context.getConfiguration().get(ConfigKeys.DOCUMENT_FILTER_CLASSES);
		if(filterClassNames != null) {
			LOG.info("Load filter classes: classNames=" + filterClassNames);
			String[] aClazz = filterClassNames.split("\\s*,\\s*");
			for(String clazz : aClazz) {
				TermFilter filter = ReflectionUtils.getInstance(
						clazz, TermFilter.class,  new Object[] { context });
				if(filter == null) {
					throw new RuntimeException("Fail to reflect: class=" + clazz);
				}
				filters.add(filter);
				LOG.info("Added filter instance: filter=" + filter);
			}
		}
	}
	
	@Override
	public void fire() {
		int labelCnt = context.getFDMetadata().getInputRootDir().list().length;
		LOG.info("Start to collect: labelCnt=" + labelCnt);
		latch = new CountDownLatch(labelCnt);
		executorService = Executors.newCachedThreadPool(new NamedThreadFactory("POOL"));
		try {
			for(String label : context.getFDMetadata().getInputRootDir().list()) {
				LOG.info("Collect words for: label=" + label);
				executorService.execute(new EachLabelWordAnalysisWorker(label));
			}
		} finally {
			try {
				latch.await();
			} catch (InterruptedException e) { }
			LOG.info("Shutdown executor service: " + executorService);
			executorService.shutdown();
		}
		
		// output statistics
		stat();
	}
	
	protected void filterTerms(Map<String, Term> terms) {
		for(TermFilter filter : filters) {
			filter.filter(terms);
		}
	}

	private void stat() {
		LOG.info("STAT: totalDocCount=" + context.getVectorMetadata().totalDocCount());
		LOG.info("STAT: labelCount=" + context.getVectorMetadata().labelCount());
		Iterator<Entry<String, Map<String, Map<String, Term>>>> iter = context.getVectorMetadata().termTableIterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Map<String, Term>>> entry = iter.next();
			Iterator<Entry<String, Map<String, Term>>> docIter = entry.getValue().entrySet().iterator();
			int termCount = 0;
			while(docIter.hasNext()) {
				termCount += docIter.next().getValue().size();
			}
			LOG.info("STAT: label=" + entry.getKey() + ", docCount=" + entry.getValue().size() + ", termCount=" + termCount);
		}
	}
	
	private final class EachLabelWordAnalysisWorker extends Thread {
		
		private final String label;
		private final DocumentAnalyzer analyzer;
		
		public EachLabelWordAnalysisWorker(String label) {
			this.label = label;
			String analyzerClass = context.getConfiguration().get(ConfigKeys.DOCUMENT_ANALYZER_CLASS);
			LOG.info("Analyzer class name: class=" + analyzerClass);
			analyzer = ReflectionUtils.getInstance(
					analyzerClass, DocumentAnalyzer.class, new Object[] { context.getConfiguration() });
		}
		
		@Override
		public void run() {
			try {
				File labelDir = new File(context.getFDMetadata().getInputRootDir(), label);
				File[] files = labelDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.getAbsolutePath().endsWith(context.getFDMetadata().getFileExtensionName());
					}
				});
				LOG.info("Prepare to analyze: label=" + label + ", totalFiles=" + files.length);
				int n = 0;
				for(File file : files) {
					analyze(label, file);
					++n;
				}
				LOG.info("Finish to analyze: label=" + label + ", fileCount=" + n);
			} finally {
				latch.countDown();
			}
		}
		
		protected void analyze(String label, File file) {
			String doc = file.getAbsolutePath();
			LOG.debug("Process document: label=" + label + ", file=" + doc);
			Map<String, Term> terms = analyzer.analyze(file);
			LOG.info(label + "," + file.getName() + "," + terms.size());
			// filter terms
			filterTerms(terms);
			// construct memory structure
			context.getVectorMetadata().addTerms(label, doc, terms);
			// add inverted table as needed
			context.getVectorMetadata().addTermsToInvertedTable(label, doc, terms);
			LOG.debug("Done: file=" + file + ", termCount=" + terms.size());
			LOG.debug("Terms in a doc: terms=" + terms);
		}
	}

}
