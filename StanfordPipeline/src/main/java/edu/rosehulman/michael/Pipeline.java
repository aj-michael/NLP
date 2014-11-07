package edu.rosehulman.michael;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Pipeline {

	public static void main(String[] args) {
		String text = "Many were increasingly of the opinion that they'd all made a big mistake in coming down from the trees in the first place. And some said that even the trees had been a bad move, and that no one should ever have left the oceans.";
		System.out.println(text);
		analyze(text);
	}
	
	public static void analyze(String text) {
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		//props.put("annotators", "tokenize, ssplit, pos");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		Annotation document = new Annotation(text);
		System.out.println(document);
		
		pipeline.annotate(document);
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.getString(TextAnnotation.class);
				String pos = token.getString(PartOfSpeechAnnotation.class);
				String ne = token.getString(NamedEntityTagAnnotation.class);
				System.out.println("------------------------");
				System.out.println(word);
				System.out.println(pos);
				System.out.println(ne);
			}
			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println(tree);
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		}
		
		Map<Integer,CorefChain> graph = document.get(CorefChainAnnotation.class);
		System.out.println(graph);
		
	}

}
