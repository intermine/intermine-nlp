(ns intermine-nlp.core-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.core :as core]
            [intermine-nlp.model :as model]
            [instaparse.core :as insta]))

(deftest parser-pipeline-test
  (let [service {:root "www.flymine.org/query"}
        db-model (model/fetch-model service)
        pipeline (core/parser-pipeline db-model :lemmatize true)])
  (testing "Testing parser-pipeline"
    (are [string] (insta/failure? (pipeline string))
        "This sentence does not parse."
        "which Genes doesn't parse."
        "Gene length for all")
      (are [string] ((complement insta/failure?) (pipeline string))
        "show me Gene with primaryIdentifier ovo")))
