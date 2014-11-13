package edu.rosehulman.summarizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

/*	Summarizes a document based on a model file generated by either
 *  Preprocess.java or PreprocessRec.java
 *  
 *  Ben, your turn.
 */
public class DocumentSummarizer {

	Counter<String> df;
	int numDocuments;
	
	private static final StanfordCoreNLP pipeline;
	private static final Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators","tokenize,ssplit,pos");
		props.setProperty("tokenize.language","en");
		props.setProperty("pos.model","edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		pipeline = new StanfordCoreNLP(props);
	}

	public DocumentSummarizer(Counter<String> df) {
		this.df = df;
		this.numDocuments = (int) df.getCount("numDocuments");
	}
	
	public String summarize(String document, int numSentences) {
		Annotation annotation = pipeline.process(document);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		Counter<String> TF = calcTF(sentences);
		sentences = rankSentencesByTFIDF(sentences, TF);
		StringBuilder summary = new StringBuilder();
		for (int i = 0; i < numSentences; i++) {
			summary.append(sentences.get(i));
			summary.append(" ");
		}
		return summary.toString();
	}
	
	public List<CoreMap> rankSentencesByTFIDF(List<CoreMap> sentences, Counter<String> tfs) {
		Collections.sort(sentences, new SentenceComparator(tfs));
		return sentences;
	}
	
	private class SentenceComparator implements Comparator<CoreMap> {
		private final Counter<String> termFrequencies;
		
		public SentenceComparator(Counter<String> termFrequencies) {
			this.termFrequencies = termFrequencies;
		}
		
		public int compare(CoreMap map1, CoreMap map2) {
			return (int) Math.round(score(map2) - score(map1));
		}
		
		private double score(CoreMap sentence) {
			int index = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
			return calcTFIDF(sentence) * 500.0 / index;
		}
		
		private double calcTFIDF(CoreMap sentence) {
			double total = 0;
			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
			for (CoreLabel token : tokens) {
				String pos = token.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (pos.startsWith("n") || pos.startsWith("N")) {
					String text = token.getString(CoreAnnotations.TextAnnotation.class);
					if (df.getCount(text) != 0) {
						double tf = 1 + Math.log(termFrequencies.getCount(text));
						double idf = Math.log(numDocuments / (1 + df.getCount(text)));
						total += tf*idf;
					}
				}
			}
			return total;
		}

	}
	
	public static Counter<String> calcTF(List<CoreMap> sentences) {
		Counter<String> frequencies = new ClassicCounter<String>();
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				frequencies.incrementCount(token.get(CoreAnnotations.TextAnnotation.class));
			}
		}
		return frequencies;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String documentPath = args[0];
		String content = IOUtils.slurpFile(documentPath);
		
		String dfPath = args[1];
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(dfPath));
		Counter<String> df = (Counter<String>) stream.readObject();
		stream.close();
		
		DocumentSummarizer summarizer = new DocumentSummarizer(df);
		System.out.println(summarizer.numDocuments);
		String result = summarizer.summarize(content, 2);
		System.out.println(result);
	}
	
}
