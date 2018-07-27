(ns intermine-nlp.model-test
  (:require [clojure.test :as test]
            [intermine-nlp.model :as model]))

(def model-names ["medic" "fly" "human" "yeast" "rat" "mouse" "fly-beta"])
(def class-counts [87 118 120 122 103 88 129])


(test/deftest fetch-model-test
  (let [models (map model/fetch-model model-names)
        fly-model (model/fetch-model "fly")]
    (test/testing "Test the fetch-model method (http requests only, no fallback)."
      (test/are [x y] (= x y)
        ;; http requests (imcljs)
        class-counts (map count models)
        fly-model (model/fetch-model "df08)*&%")
        fly-model (model/fetch-model "")
        fly-model (model/fetch-model 0)
        ))))

(test/deftest local-model-test
  (let [dummy-model {:Gene {:attributes 0 :collections 0} :GOTerm {:collections 0}}]
    (test/testing "Test the fetch-model method (http requests only, no fallback)."
      (test/are [x y] (= x y)
        ;; local fallback
        dummy-model (do
                      (model/store-model dummy-model "dummy-model")
                      (model/load-local-model "dummy-model"))
        nil (model/load-local-model "non-existant_model.edn")
        ))))
