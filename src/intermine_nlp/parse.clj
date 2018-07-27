(ns intermine-nlp.parse
  (:require [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.nlp :as nlp]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:gen-class))

(def grammar (-> "grammar.bnf" io/resource io/input-stream slurp))

(defn model-grammar
  "Generate a grammar (parse map) for an intermine model.
  Consists of 2 productions: one for classes, one for all fields/attributes.
  "
  ([model]
   (let [classes (:classes model)
         class-kws (keys classes)
         class-paths (map name class-kws)
         class-lemma-map (nlp/lemma-map (string/join " " class-paths))
         attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
         attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
         attrs (map name attr-keys)]
     (merge
      {:FIELD (apply alt (map #(string %) attrs))}
      {:CLASS (apply alt (map #(string %) class-paths))})))

  ([model class-lemma-map]
   (let [class-paths (keys class-lemma-map)
         attr-map (map #(hash-map :class % :attrs (im-path/attributes model %)) class-paths)
         attr-keys (distinct (flatten (map #(->> % :attrs keys)  attr-map)))
         attrs (map (comp nlp/lemmatize-as-text name) attr-keys)]
     (merge
      {:FIELD (apply alt (map #(string %) attrs))}
      {:CLASS (apply alt (map #(string %) (vals class-lemma-map)))}))))

(defn gen-parser
  "Generate an instaparse parser object by merging a top-level parser (default: grammar.bnf)
  with a model-specific parser.
  "
  ([model]
   (let [top-grammar (ebnf grammar)
         bottom-grammar (model-grammar model)]
     (insta/parser (merge top-grammar bottom-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true)))
  ([model class-lemma-map]
   (let [top-grammar (ebnf grammar)
         bottom-grammar (model-grammar model class-lemma-map)]
     (insta/parser (merge top-grammar bottom-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true)))
  ([model top-grammar class-lemma-map]
   (let [bottom-grammar (model-grammar model class-lemma-map)]
     (insta/parser (merge top-grammar bottom-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true))))

;;; Parse Tree transformations
(def view-map
  {:ORGANISM (fn [text] {:from text})
   :CLASS (fn [text] {:select text})
   :FIELD (fn [text] {:select text})})

(def constraint-map
  {:CLASS (fn [text] [:from text])
   :FIELD (fn [text] [:select text])})

(def transform-map
  {:QUERY (fn [& children] (remove string? children))
  :VIEW (fn [& children] (insta/transform view-map children))
   :CONSTR (fn [& children] [:CONSTR (remove string? children)])
   :VALUE (fn [text] [:VALUE text])})

(defn transform-tree
  "Transform a parse tree according "
  [parse-tree]
  (insta/transform transform-map parse-tree))
