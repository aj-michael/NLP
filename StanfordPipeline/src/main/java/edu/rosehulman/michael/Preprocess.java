package edu.rosehulman.michael;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//import com.google.common.collect.Sets;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

public class Preprocess {
	
	private static final StanfordCoreNLP pipeline;
	private static final Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators","tokenize,ssplit,pos");
		props.setProperty("tokenize.language","en");
		props.setProperty("pos.model","edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		pipeline = new StanfordCoreNLP(props);
	}

	public static void main(String[] args) throws IOException {
		String trainingDataDir = args[0];
		String outputPath = args[1];
		Counter<String> dfCounter = new ClassicCounter<String>();
		for (File f : (new File(trainingDataDir)).listFiles()) {
			String text = IOUtils.slurpFile(f);
			Annotation document = new Annotation(text);
			Preprocess.pipeline.annotate(document);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			Set<String> wordsInDoc = new HashSet<String>();
			for (CoreMap sentence : sentences) {
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					wordsInDoc.add(token.getString(TextAnnotation.class));
				}
			}

			dfCounter.incrementCount("numDocuments");

			for (String word : wordsInDoc) {
				dfCounter.incrementCount(word);
			}
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(outputPath));
		stream.writeObject(dfCounter);
		stream.close();
	}

}