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
    // public static void main(String[] args) {
    //     System.out.println("Starting Stanford Lemmatizer");
    //     String text = "How could you be seeing into my eyes like open doors? \n"+
    //             "You led me down into my core where I've became so numb \n"+
    //             "Without a soul my spirit's sleeping somewhere cold \n"+
    //             "Until you find it there and led it back home \n"+
    //             "You woke me up inside \n"+
    //             "Called my name and saved me from the dark \n"+
    //             "You have bidden my blood and it ran \n"+
    //             "Before I would become undone \n"+
    //             "You saved me from the nothing I've almost become \n"+
    //             "You were bringing me to life \n"+
    //             "Now that I knew what I'm without \n"+
    //             "You can've just left me \n"+
    //             "You breathed into me and made me real \n"+
    //             "Frozen inside without your touch \n"+
    //             "Without your love, darling \n"+
    //             "Only you are the life among the dead \n"+
    //             "I've been living a lie, there's nothing inside \n"+
    //             "You were bringing me to life.";
    //     StanfordLemmatizer slem = new StanfordLemmatizer();
    //     System.out.println(slem.lemmatize(text));
    // }

}
