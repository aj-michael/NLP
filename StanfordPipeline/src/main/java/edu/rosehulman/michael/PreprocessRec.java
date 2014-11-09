package edu.rosehulman.michael;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Sets;

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

public class PreprocessRec {
	
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
		for (File f1 : (new File(trainingDataDir)).listFiles()) {
			for (File f2 : f1.listFiles()) {
			String text = IOUtils.slurpFile(f2);
			Annotation document = new Annotation(text);
			PreprocessRec.pipeline.annotate(document);
				List<CoreMap> sentences = document
						.get(SentencesAnnotation.class);
				Set<String> wordsInDoc = Sets.newHashSet();
				for (CoreMap sentence : sentences) {
					for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
						wordsInDoc.add(token.getString(TextAnnotation.class));
					}
				}
				for (String word : wordsInDoc) {
					dfCounter.incrementCount(word);
				}
				dfCounter.incrementCount("numDocuments");
			}
		}
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(outputPath));
		stream.writeObject(dfCounter);
		stream.close();
	}

}