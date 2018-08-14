(ns intermine-nlp.fuzzy
  (:require [clojure.pprint :refer [pprint]]
            [clj-fuzzy.metrics :as fuzzy]
            [clojure.string :as string]
            [intermine-nlp.util :as util]
            [clojure.math.combinatorics :as comb]
            )
  (:gen-class))

(defn best-match
    "For a given string and dictionary, return a tuple of [word, score] representing
  the highest-scored match. Uses sorensen algorithm for fuzzy matching.
  Example: (best-match 'Manager' ['martian' 'manatee' 'bison'])
         -> ['manatee' 0.5]
  "
  ([text dictionary]
   (let [match (apply max-key #(fuzzy/sorensen text %) dictionary)]
     [match (fuzzy/sorensen text match)])))

(defn best-match-pred
  "Returns a function which returns best-match word if score is over threshold,
  otherwise returns original string.
  Example: (filter (best-match-pred ['apple' 'banana'] 0.5) ['applause'])
         -> 'apple'
           (filter (best-match-pred ['apple' 'banana'] 0.7) ['applause'])
         -> 'applause'"
  [dictionary threshold]
  #(let [[match score] (best-match % dictionary)]
     (if (>= score threshold)
       match
       %)))

(defn replace-fuzzy
  "Replace all words in a string which match with minimum threshold certainty (0.0-1.0)
  to their matching partner in a list.
  Example: (replace-fuzzy 'These are amazing words' ['amaze' 'wards'] 0.5)
         -> 'These are amaze wards"
  [text dictionary threshold]
  (try
    (let [split-string (string/split text #" ")
          match-pred (best-match-pred dictionary threshold)]
      (string/join " " (map match-pred split-string)))
    (catch Exception e nil)))

(defn replace-class-names
  "Using fuzzy logic, replace all words in text with the best-matching class
  name, if confidence threshold is exceeded."
  [model threshold text]
  (let [class-names (util/class-names model)]
    (replace-fuzzy text class-names threshold)))

(defn replace-field-names
  "Using fuzzy logic, replace all words in text with the best-matching field
  name, if confidence threshold is exceeded. If class provided, match
  only fields for given class in model."
  ([model threshold text]
   (let [field-names (util/field-names model)]
     (replace-fuzzy text field-names threshold)))
  ([model class threshold text]
   (let [field-names (util/field-names model class)]
     (replace-fuzzy text field-names threshold))))
