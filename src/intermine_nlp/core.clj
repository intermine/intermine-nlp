(ns intermine-nlp.core
  (:require [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]])
  (:gen-class))


(defn read-sentence
  "Read a sentence from a file (for testing)"
  [path]
  (-> path slurp read-string :english))

(defn read-pathquery
  "Read a PathQuery from a file (for testing)"
  [path]
  (-> path slurp read-string :pathquery))


(defn parser-pipeline
  "Generate a parser pipeline for a given InterMine model.
  options:
         :lemmatize (default = false)"
  [model & {:as options}]
  (let [class-lemma-map (->> model
                             :classes
                             keys
                             (map name)
                             (clojure.string/join " ")
                             nlp/lemma-map)
        parser (parse/gen-parser model)
        parser-lemmatized (parse/gen-parser model class-lemma-map)]
    #(cond->> %
        (:lemmatize options) nlp/lemmatize-as-text
        (:lemmatize options) parser-lemmatized
        (not (:lemmatize options)) parser
        ;; (:lemmatize options) transform tree
        )))

(defn -main
  "In the future I'll return a query, right now I'll just give you the parse tree."
  [& args]
  (let [fly-model (model/fetch-model "fly")
        pipeline (parser-pipeline fly-model :lemmatize true)]
    (pprint "Enter a simple query and I'll attempt to parse it!")
    (pprint "Example: Show me genes with primaryIdentifier like ovo.")
    (loop []
      (print "\n")
      (pprint "To quit, hit <ENTER>")
      (let [text (read-line)
            result (pipeline text)]
        (if (not (empty? text))
          (do (cond
                (insta/failure? result) (pprint "Sorry, I couldn't parse that.")
                :else                   (pprint result))
              (recur))
          (pprint "Bye! Happy hacking."))))))
