(ns intermine-nlp.fuzzy-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.model :as model]
            [intermine-nlp.fuzzy :as fuzzy]))

(def db-model (model/fetch-model "fly"))
(def service {:root "www.flymine.org/query"
              :model db-model})

(deftest replace-class-names-test
  (testing "Testing replace-class-names"
    (are [string replacement]
        (= replacement (fuzzy/replace-class-names db-model 0.6 string))
      "A chromosomal demo" "A Chromosome demo"
      "Which genes have do-term foo?" "Which Gene have DOTerm foo?"
      )
    (are [string] (= string (fuzzy/replace-class-names db-model 0.7 string))
      "No classes here."
      "Close, but no cigar")))

(deftest replace-field-names-test
  (testing "Testing replace-field-names"
    (are [string replacement]
        (= replacement (fuzzy/replace-field-names db-model 0.6 string))
      "annotatextension" "annotationExtension"
      "Show me genes having primaryID ovo" "Show me genes having primaryIdentifier ovo")
    (are [string] (= string (fuzzy/replace-field-names db-model 0.6 string))
      "No fields here."
      "Close, but no cigar"
      "Genetic")))

