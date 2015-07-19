package org.shirdrn.document.preprocessing.api.constants;

public interface ConfigKeys {

	String DATASET_FILE_CHARSET = "dataset.file.charset";
	String DATASET_TRAIN_FILE_EXTENSION = "dataset.train.file.extension";
	String DATASET_FEATURE_TERM_VECTOR_FILE = "dataset.feature.term.vector.file";
	String DATASET_LABEL_VECTOR_FILE = "dataset.label.vector.file";
	
	String DATASET_TRAIN_INPUT_ROOT_FILE = "dataset.train.input.root.dir";
	String DATASET_TRAIN_SVM_VECTOR_FILE = "dataset.train.svm.vector.file";
	String DATASET_TRAIN_SVM_VECTOR_OUTPUT_DIR = "dataset.train.svm.vector.output.dir";
	
	String DATASET_TEST_INPUT_ROOT_FILE = "dataset.test.input.root.dir";
	String DATASET_TEST_SVM_VECTOR_FILE = "dataset.test.svm.vector.file";
	String DATASET_TEST_SVM_VECTOR_OUTPUT_DIR = "dataset.test.svm.vector.output.dir";
	
	String DOCUMENT_ANALYZER_CLASS = "document.analyzer.class";
	String DOCUMENT_ANALYZER_STOPWORDS_PATH = "document.analyzer.stopwords.path";
	String DOCUMENT_FILTER_CLASSES = "document.filter.classes";
	String DOCUMENT_FILTER_KEPT_LEXICAL_CATEGORIES = "document.filter.kept.lexical.categories";
	
	String FEATURE_VECTOR_SELECTOR_CLASS = "feature.vector.selector.class";
	String FEATURE_EACH_LABEL_KEPT_TERM_PERCENT = "feature.each.label.kept.term.percent";
}
