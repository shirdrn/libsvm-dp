package org.shirdrn.document.preprocessing.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.shirdrn.document.preprocessing.api.ConfigReadable;
import org.shirdrn.document.preprocessing.api.DocumentAnalyzer;
import org.shirdrn.document.preprocessing.api.Term;
import org.shirdrn.document.preprocessing.common.AbstractDocumentAnalyzer;
import org.shirdrn.document.preprocessing.common.TermImpl;

import com.google.common.collect.Maps;

public class LuceneDocumentAnalyzer extends AbstractDocumentAnalyzer implements DocumentAnalyzer {

	private static final Log LOG = LogFactory.getLog(LuceneDocumentAnalyzer.class);
	private final Analyzer analyzer;
	
	public LuceneDocumentAnalyzer(ConfigReadable configuration) {
		super(configuration);
		analyzer = new SmartChineseAnalyzer(false);
	}

	@Override
	public Map<String, Term> analyze(File file) {
		String doc = file.getAbsolutePath();
		LOG.debug("Process document: file=" + doc);
		Map<String, Term> terms = Maps.newHashMap();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charSet));
			String line = null;
			while((line = br.readLine()) != null) {
				LOG.debug("Process line: " + line);
				TokenStream ts = analyzer.tokenStream("", new StringReader(line));
				ts.reset();
				ts.addAttribute(CharTermAttribute.class); 
				while (ts.incrementToken()) {  
					CharTermAttributeImpl attr = (CharTermAttributeImpl) ts.getAttribute(CharTermAttribute.class);  
					String word = attr.toString().trim();
					if(!word.isEmpty() && !super.isStopword(word)) {
						Term term = terms.get(word);
						if(term == null) {
							term = new TermImpl(word);
							terms.put(word, term);
						}
						term.incrFreq();
					} else {
						LOG.debug("Filter out stop word: file=" + file + ", word=" + word);
					}
					ts.end();
				}
				ts.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("", e);
		} finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				LOG.warn(e);
			}
			LOG.debug("Done: file=" + file + ", termCount=" + terms.size());
		}
		return terms;
	}

}
