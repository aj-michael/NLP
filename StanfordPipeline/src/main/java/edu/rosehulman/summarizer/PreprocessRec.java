package edu.rosehulman.summarizer;

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
	
	public static Counter<String> calcDF(String outerTrainingDir) throws IOException {
		Counter<String> df = new ClassicCounter<String>();
		for (File category : (new File(outerTrainingDir)).listFiles()) {
			for (File document : category.listFiles()) {
				String text = IOUtils.slurpFile(document);
				Annotation annotatedDocument = new Annotation(text);
				PreprocessRec.pipeline.annotate(annotatedDocument);
				List<CoreMap> sentences = annotatedDocument.get(SentencesAnnotation.class);
				Set<String> wordsInDoc = Sets.newHashSet();
				for (CoreMap sentence : sentences) {
					for (CoreLabel word : sentence.get(TokensAnnotation.class)) {
						wordsInDoc.add(word.getString(TextAnnotation.class));
					}
				}
				for (String word : wordsInDoc) {
					df.incrementCount(word);
				}
				df.incrementCount("numDocuments");
			}
		}
		return df;
	}

	public static void main(String[] args) throws IOException {
		String trainingDataDir = args[0];
		String outputPath = args[1];
		Counter<String> df = calcDF(trainingDataDir);
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(outputPath));
		stream.writeObject(df);
		stream.close();
	}

}