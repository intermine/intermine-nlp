(ns intermine-nlp.core-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.core :as core]
            [intermine-nlp.model :as model]))

(deftest parser-pipeline-test
  (let [service {:root "www.flymine.org/query"}
        db-model (model/fetch-model service)
        service (assoc service :model db-model)
        pipeline (core/parser-pipeline service)]
    (testing "Testing parser-pipeline"
      (are [string] (every? empty? (vals (pipeline string)))
        "This sentence does not parse."
        "which Genes doesn't parse."
        "Gene length for all")
      (are [string] (not-any? empty? (vals (pipeline string)))
        "Show me genes with primaryIdentifier ovo"))))
