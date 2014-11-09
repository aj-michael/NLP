package edu.rosehulman.michael;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Sets;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

public class DocumentSummarizer {

	Counter<String> dfCounter;
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

	public DocumentSummarizer(Counter<String> dfCounter) {
		this.dfCounter = dfCounter;
		this.numDocuments = (int) dfCounter.getCount("__all__");
	}
	
	public String summarize(String document, int numSentences) {
		Annotation annotation = pipeline.process(document);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		Counter<String> tfs = getTermFrequencies(sentences);
		sentences = rankSentences(sentences, tfs);
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < numSentences; i++) {
			ret.append(sentences.get(i));
			ret.append(" ");
		}
		return ret.toString();
	}
	
	public List<CoreMap> rankSentences(List<CoreMap> sentences, Counter<String> tfs) {
		Collections.sort(sentences, new SentenceComparator(tfs));
		return sentences;
	}
	
	private class SentenceComparator implements Comparator<CoreMap> {
		private final Counter<String> termFrequencies;
		
		public SentenceComparator(Counter<String> termFrequencies) {
			this.termFrequencies = termFrequencies;
		}
		
		public int compare(CoreMap o1, CoreMap o2) {
			return (int) Math.round(score(o2) - score(o1));
		}
		
		private double score(CoreMap sentence) {
			double tfIdf = tfIDFWeights(sentence);
			int index = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
			double indexWeight = 5.0 / index;
			return indexWeight * tfIdf * 100;
		}
		
		private double tfIDFWeights(CoreMap sentence) {
			double total = 0;
			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
			for (CoreLabel cl : tokens) {
				String pos = cl.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
				boolean isNoun = pos.startsWith("n");
				if (isNoun) {
					String text = cl.getString(CoreAnnotations.TextAnnotation.class);
					if (dfCounter.getCount(text) != 0) {
						double tf = 1 + Math.log(termFrequencies.getCount(text));
						double idf = Math.log(numDocuments / (1 + dfCounter.getCount(text)));
						total += tf*idf;
					}
				}
			}
			return total;
		}

	}
	
	public static Counter<String> getTermFrequencies(List<CoreMap> sentences) {
		Counter<String> ret = new ClassicCounter<String>();
		for (CoreMap sentence : sentences) {
			for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				ret.incrementCount(cl.get(CoreAnnotations.TextAnnotation.class));
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		String documentPath = args[0];
		String content = IOUtils.slurpFile(documentPath);
		
		String dfCounterPath = args[1];
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(dfCounterPath));
		Counter<String> dfCounter = (Counter<String>) stream.readObject();
		
		DocumentSummarizer summarizer = new DocumentSummarizer(dfCounter);
		String result = summarizer.summarize(content, 2);
		System.out.println(result);
	}
	
}
