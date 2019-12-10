(ns intermine-nlp.parse-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.parse :as parse]
            [intermine-nlp.model :as model]
            [intermine-nlp.randquery :as randq]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [imcljs.path :as im-path]
            [clojure.set :refer [map-invert]]
            [intermine-nlp.nlp :as nlp]))

(def db-model (model/fetch-model "fly"))
(def service {:root "www.flymine.org/query"
              :model db-model})

(def lemma-class-map (map-invert (parse/field-lemma-mapping db-model)))
(def lemma-field-map (map-invert (parse/class-lemma-mapping db-model)))

(deftest gen-parser-test
  ;; TODO: parse collection of human-generated queries
  (let [parser1 (parse/gen-parser db-model)]
    (testing "testing gen-parser function"
      (are [string] (insta/failure? (insta/parse parser1 string))
        "This sentence does not parse."
        "which Genes doesn't parse."
        "Gene length for all")
      (are [string] ((complement insta/failure?) (insta/parse parser1 string))
        "show me gene with primaryidentifier ovo!"
        "find all drosophila allele with organism name like ' foo.bar.baz ' ."
        "which drosophila allele have organism name like ' foo bar baz ' ?"))))
