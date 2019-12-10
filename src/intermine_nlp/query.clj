(ns intermine-nlp.query
  (:require [intermine-nlp.parse :as parse]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]]
            [intermine-nlp.util :as util]
            [clojure.string :as string]
            [intermine-nlp.fuzzy :as fuzzy])
  (:gen-class))

(def op-map {
             :EQ "="
             :NEQ "!="
             :GT ">"
             :LT "<"
             :GEQ ">="
             :LEQ "<="
             :LIKE "LIKE"
             :NLIKE "NOT LIKE"
             :CONTAINS "CONTAINS"
             :IN "IN"
             :NIN "NOT IN"
             :M_ONE_OF "ONE OF"
             :M_NONE_OF "NONE OF"
             :M_OVERLAPS "OVERLAPS"
             :M_NOVERLAPS "DOES NOT OVERLAP"
             :M_OUTSIDE "OUTSIDE"
             :M_WITHIN "WITHIN"
             :M_CONTAINS "CONTAINS"
             :M_NCONTAINS "DOES NOT CONTAIN"
             :NULL "IS NULL"
             :NNULL "IS NOT NULL"})

(defn gen-summary
  "Generate a summary view for a given class path (returns nil if unrecognized class).
  Do not use lemmatized class name."
  [service path]
  (util/class-summary service path))

(defn gen-root
  "Generate the root path string based on the :PATH->:CLASS of a view.
  If no root, return nil."
  [view lemma-class-map]
  (let [classes (distinct (flatten (map #(get-in % [:PATH :CLASS]) view)))
        class-paths (distinct (map #(util/un-lemmatize % lemma-class-map) classes))]
    (some identity class-paths)))

(defn gen-path
  "Merge the values in a map of view elements (:CLASS, :FIELD) into an
  imcljs path map."
  [model path root lemma-class-map lemma-field-map]
  (let [class-name (util/un-lemmatize (:CLASS path) lemma-class-map)
        class (try (if (= class-name root) root (string/lower-case class-name))
                   (catch Exception e root))
        field-name (util/un-lemmatize (:FIELD path) lemma-field-map)
        field (cond
                class-name (fuzzy/replace-field-names model class-name 0.1 field-name)
                class (fuzzy/replace-field-names model root 0.3 field-name)
                field-name (fuzzy/replace-field-names model 0.3 field-name))]
    (cond
      (and (not-empty class) (not-empty field)) (str class "." field)
      (not-empty class) class
      (not-empty field) field)))

(defn gen-view
  "Merge the values in a seq of :PATH elements into an imcljs view map.
  If no view, return nil."
  [service paths root lemma-class-map lemma-field-map]
  (let [view (seq
              (map #(gen-path (:model service) (:PATH %) root lemma-class-map lemma-field-map)
                   paths))]
    (cond
      (= view [root]) (gen-summary service root)
      :else           view)))
 
(defn gen-constraint
  "Merge the values in a map of constraint elements(:CLASS, :FIELD, :VALUE,
  :COMPARE, :MULTI_COMPARE, :UNARY_OP) into an imcljs constraint map."
  [model constraint root lemma-class-map lemma-field-map]
  (let [path (gen-path model (:PATH constraint) root lemma-class-map lemma-field-map)
        compare (get op-map (:COMPARE constraint))
        multi-compare (get op-map (:MULTI_COMPARE constraint))
        unary-op (get op-map (:UNARY_OP constraint))
        value (:VALUE constraint)
        values (:VALUES constraint)]
    (cond
      compare {:path path
               :op compare
               :value value}
      multi-compare {:path path
                     :op compare
                     :value values}
      unary-op {:path path
                :op unary-op}
      value    {:path path
                :op "="
                :value value}
      values   {:path path
                :op "ONE OF"
                :value values}
      :else    {:value "ERROR"}
    )))

(defn gen-constraints
  "Merge the values in a seq of :CONSTR elements into an imcljs constraints map.
  If no constraints, return nil."
  [model constraints root lemma-class-map lemma-field-map]
  (seq
   (map #(gen-constraint model (:CONSTR %) root lemma-class-map lemma-field-map) constraints)))

(defn gen-query
  "Generate a query from a map containing :VIEW :CONSTRS and :ORGANISM keys."
  [service parse-map]
  (let [model (:model service)
        lemma-class-map (map-invert (parse/class-lemma-mapping model))
        lemma-field-map (map-invert (parse/field-lemma-mapping model))
        paths (:VIEW parse-map)
        constraints (:CONSTRS parse-map)
        root (gen-root paths lemma-class-map)]
    {:from root
     :select (gen-view service paths root lemma-class-map lemma-field-map)
     :where (gen-constraints model constraints root lemma-class-map lemma-field-map)
     }))
