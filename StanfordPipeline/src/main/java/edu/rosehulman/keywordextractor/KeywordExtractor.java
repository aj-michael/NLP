package edu.rosehulman.keywordextractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

public class KeywordExtractor {

	Counter<String> df;
	int N;
	
	private static final StanfordCoreNLP pipeline;
	private static final Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators","tokenize,ssplit,pos");
		props.setProperty("tokenize.language", "en");
		props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public KeywordExtractor(Counter<String> df) {
		this.df = df;
		this.N = (int) df.getCount("numDocuments");
	}
	
	public List<String> extractKeywords(String document, int num) {
		Annotation annotation = pipeline.process(document);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		Counter<String> TF = new ClassicCounter<String>();
		Set<String> nounSet = new HashSet<String>();
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				TF.incrementCount(token.get(CoreAnnotations.TextAnnotation.class));
				String partOfSpeech = token.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (partOfSpeech.toLowerCase().startsWith("n") && !document.split("\n")[0].contains(token.getString(CoreAnnotations.TextAnnotation.class))) {
					nounSet.add(token.get(CoreAnnotations.TextAnnotation.class));
				}
			}
		}
		List<String> nouns = new ArrayList<String>(nounSet);
		Collections.sort(nouns, new TermComparator(TF));
		return nouns.subList(0, num);
	}
	
	private class TermComparator implements Comparator<String> {

		private final Counter<String> TF;
		
		public TermComparator(Counter<String> TF) {
			this.TF = TF;
		}
		
		@Override
		public int compare(String term1, String term2) {
			return (int) Math.round(calcTFIDF(term2) - calcTFIDF(term1));
		}
		
		private double calcTFIDF(String term) {
			double tf = 1 + Math.log(TF.getCount(term));
			double idf = Math.log(N / (1 + df.getCount(term)));
			return tf * idf;
		}
		
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String documentPath = args[1];
		String content = IOUtils.slurpFile(documentPath);
		int numKeywords = Integer.parseInt(args[2]);
		
		String dfPath = args[0];
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(dfPath));
		Counter<String> df = (Counter<String>) stream.readObject();
		stream.close();
		
		KeywordExtractor extractor = new KeywordExtractor(df);
		List<String> keywords = extractor.extractKeywords(content, numKeywords);
		for (String keyword : keywords) {
			System.out.println(keyword);
		}
	}

}
