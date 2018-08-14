(ns intermine-nlp.parse
  (:require [clojure.core.match :refer [match]]
            [instaparse.core :as insta]
            [instaparse.combinators :refer :all]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [intermine-nlp.nlp :as nlp]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [intermine-nlp.util :as util]
            [intermine-nlp.fuzzy :as fuzzy])
  (:gen-class))

(def grammar (-> "grammar.bnf" io/resource io/input-stream slurp))

(defn class-lemma-mapping
  "Given a model, lemmatizes all class names and returns a hash map,
  mapping lemmatized class names to un-lemmatized ones."
  [model]
  (let [class-paths (util/class-names model)]
    (->> class-paths
         (clojure.string/join " ")
         nlp/lemma-map)))

(defn field-lemma-mapping
  "Given a model, lemmatizes all field names and returns a hash map,
  mapping lemmatized fields to un-lemmatized ones."
  [model]
  (let [field-paths (util/field-names model)]
    (->> field-paths
         (clojure.string/join " ")
         nlp/lemma-map)))

(defn model-grammar
  "Generate a grammar (parse map) for an intermine model.
  Consists of 2 productions: one for classes, one for all fields/attributes.
  "
  ([model]
   (let [class-lemma-map (class-lemma-mapping model)
         class-paths (keys class-lemma-map)
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
   (let [top-grammar (ebnf grammar)]
     (gen-parser model top-grammar)))
  ([model top-grammar]
   (let [bottom-grammar (model-grammar model)]
     (insta/parser (merge top-grammar bottom-grammar)
                   :start :QUERY
                   :auto-whitespace :standard
                   :string-ci true))))


;;; Parse Tree transformations
(def view-map
  {:PATH  (fn [& children] {:PATH (apply merge (remove string? children))})
   :CLASS (fn [text] {:CLASS text})
   :FIELD (fn [text] {:FIELD text})
   :VALUE (fn [text] {:VALUE text})
   })

(defn transform-view
  "Transform :VIEW map into a format recognizable by imcljs"
  [view-tree]
  (let [tree (insta/transform view-map view-tree)]
    (->> tree
         (remove string?)
         flatten
         (remove string?))))

(def constraint-map
  {:CONSTR (fn [& constr] {:CONSTR (apply merge constr)})
   :PATH  (fn [& path] {:PATH (apply merge path)})
   :CLASS (fn [text] {:CLASS text})
   :FIELD (fn [text] {:FIELD text})
   :VALUE (fn [text] {:VALUE text})
   :VALUES (fn [& values] {:VALUES (filter identity (map :VALUE values))})
   :COMPARE (fn [comparison] {:COMPARE (first (second comparison))})
   :MULTI_COMPARE (fn [comparison] {:MULTI_COMPARE (first comparison)})
   :UNARY_OP (fn [op] {:UNARY_OP (first op)})
   })

(defn transform-constraints
  "Transform :CONSTR map into a format recognizable by imcljs"
  [constr-tree]
  (let [tree (insta/transform constraint-map constr-tree)]
    (->> tree
         flatten
         (remove string?)
         ;; (apply merge)
         )))

(def top-map
  {:QUERY (fn [& children] (remove string? children))
   :ORGANISM (fn [text] {:ORGANISM text})
   :VIEW (fn [& children] {:VIEW
                          (vec (transform-view (remove string? children)))})
   :CONSTRS (fn [& children] {:CONSTRS
                             (vec (transform-constraints (remove string? children)))})
   ;; :SORT (fn [& children] {:SORT
   ;;                        (vec (transform-sort (remove string? children)))})
   })

(defn transform-tree
  "Transform a parse tree according "
  [parse-tree]
  (apply merge (flatten (insta/transform top-map parse-tree))))
