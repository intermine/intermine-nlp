(ns intermine-nlp.model-test
  (:require [clojure.test :as test]
            [intermine-nlp.model :as model]))

(def model-root-map {"fly" {:root "www.flymine.org/query"}
                     "medic" {:root "medicmine.jcvi.org/medicmine"}
                     "human" {:root "www.humanmine.org/humanmine"}
                     "yeast" {:root "yeastmine.yeastgenome.org/yeastmine"}
                     "rat" {:root "ratmine.mcw.edu/ratmine"}
                     "mouse" {:root "www.mousemine.org/mousemine"}
                     "fly-beta" {:root "beta.flymine.org/beta"}})

(defn setup-local-models
  "Setup tests by fetching and locally storing all 7 tested database models."
  [test-fn]
  (println "Fetching models for tests...")
  (doall (map #(model/store-model (model/fetch-model % :no-fallback true) %)
              (keys model-root-map)))
  (test-fn))

(test/use-fixtures :once setup-local-models)

(test/deftest fetch-model-test
  (test/testing "Test the fetch-model method."
    (test/are [db-kw] (= (model/fetch-model db-kw)
                      (model/fetch-model (get model-root-map db-kw))
                      (model/load-local-model db-kw))
      "fly"
      "medic"
      "human"
      "yeast"
      "rat"
      "mouse"
      "fly-beta"
      )
    (test/are [db-kw] (= #{:classes :name :version :package}
                         (set (keys (model/fetch-model db-kw))))
      "fly"
      "medic"
      "human"
      "yeast"
      "rat"
      "mouse"
      "fly-beta")))

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
