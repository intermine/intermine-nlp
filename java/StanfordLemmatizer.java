// Author: Bruno Bonacci
// https://github.com/BrunoBonacci/lemma
// License: MIT

package lemmatizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordLemmatizer {

    protected StanfordCoreNLP pipeline;

    public StanfordLemmatizer() {
        Properties cfg;
        cfg = new Properties();
        cfg.put("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(cfg);
    }

    public List<List<String>> lemmatize(String text)
    {
        List<List<String>> lines = new ArrayList<List<String>>();
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        List<CoreMap> sentences = document.get( SentencesAnnotation.class );
        for(CoreMap sentence: sentences) {
            List<String> words = new ArrayList<String>();
            for (CoreLabel token: sentence.get( TokensAnnotation.class )) {
                words.add( token.get( LemmaAnnotation.class ) );
            }
            lines.add( words );
        }
        return lines;
    }
}
