(ns intermine-nlp.nlp
  (:require [opennlp.nlp :as onlp]
            [opennlp.treebank :as treebank]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import  [lemmatizer StanfordLemmatizer]))


;;; Apache OpenNLP
;; (def en-sent-file (-> "en-sent.bin" clojure.java.io/resource .getFile))
(def en-sent-file (-> "en-sent.bin" io/resource io/input-stream))
(def en-token-file (-> "en-token.bin" io/resource io/input-stream))
(def en-pos-maxent-file (-> "en-pos-maxent.bin" io/resource io/input-stream))
(def en-chunker-file (-> "en-chunker.bin" io/resource io/input-stream))
(def en-parser-chunking-file (-> "en-parser-chunking.bin" io/resource io/input-stream))

(def get-sentences (onlp/make-sentence-detector en-sent-file))
(def tokenize
  "Split a string into words and punctuation marks.
  Example:
         'This is a sentence.' -> '('this' 'is' 'a' 'sentence')"
  (comp (partial remove #(= % "'s"))
          (onlp/make-tokenizer en-token-file)))
(def pos-tag (onlp/make-pos-tagger en-pos-maxent-file))
(def chunker (treebank/make-treebank-chunker en-chunker-file))
(def parser (treebank/make-treebank-parser en-parser-chunking-file))

(defn prepare-nlquery
  "Raw natural language queries must end in a period for some OpenNLP functions"
  [text]
  (let [last-char (subs text (dec (count text)))]
    (if
        (or
         (= last-char ".")
         (= last-char "?")
         (= last-char "!")) text
        (str text "."))))

(defn nlquery-to-chunk
  "Use NLP to convert raw text to chunked form"
  [text]
  (-> text prepare-nlquery tokenize pos-tag chunker vec))

(defn nlquery-to-tree
  "Use NLP to convert raw text to treebank tree form"
  [text]
  (-> text prepare-nlquery vector parser first treebank/make-tree))



;;; Stanford CoreNLP
(defonce core-nlp (delay (StanfordLemmatizer.)))


(defn lemmatize
  "Lemmatize a string, converting words to their canonical/infinitive forms.
  Example: 'These are many words' -> 'these be many word. Returns a vector of strings
  (one string per word). Removes possessive 's'."
  [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp]
    (->> (.lemmatize nlp text)
         vec
         (map (partial remove #(= % "'s")))
         (map vec))))

(defn lemmatize-as-text
    "Lemmatize a string, converting words to their canonical/infinitive forms.
  Example: 'These are many words' -> 'these be many word. Returns a flattened string"
  [^String text]
  (->> (lemmatize text)
       (map (partial string/join " "))
       (string/join " ")))

(defn lemma-map
  "TODO: replace tokenization with regex (tokenize fails on conjunctions like it's)
  Lemmatize a string, returning a word map between the input words and their
  lemmatized form.
  Example: 'It's nice meeting you' -> {'It' 'it', 'nice' 'be', 'meeting' 'nice', 'you' 'meeting'}"
  [^String text]
  (let [^StanfordLemmatizer nlp @core-nlp
        tokens (tokenize text)]
    (->> text
         lemmatize
         flatten
         (zipmap tokens)
         )))
