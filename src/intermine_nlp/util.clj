(ns intermine-nlp.util
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [imcljs.fetch :as fetch]
            [imcljs.path :as path]
            [clojure.core.async :refer [<! go]]
            [clojure.string :refer [join split]]
            [clj-http.client :as http]
            [clojure.string :as str]))

(defn read-edn
  "Read an edn file into a map"
  [path]
  (try (with-open [file (io/reader path)]
           (-> file java.io.PushbackReader. edn/read))
       (catch Exception e (printf "Couldn't open file '%s'." path))))

(defn pseudo-possible-values-
  "imcljs.fetch/possible-values seems to be broken, so here's an implementation
  using only imcljs.fetch/rows. 'path' must point to a field, not a class."
  [service path]
  (let [split-path (split (path/adjust-path-to-last-class (:model service) path) #"\.")
        query {:from (first split-path)
               :select [(last split-path)]}
        rows (try (fetch/rows service query)
                  (catch java.lang.Exception e nil))]
       (->> rows :body :results (map first) distinct)))

(def psuedo-possible-values
    "imcljs.fetch/possible-values seems to be broken, so here's an implementation
  using only imcljs.fetch/rows. 'path' must point to a field, not a class."
  (memoize pseudo-possible-values-))

(defn possible-values-
  "Fetch possible values using the clj-http library, as the imcljs implementations
  of possible-values and unique-values are broken"
  [service path]
  (let [request-path (str "http://" (:root service) "/service/path/values")
        request-params {:query-params {"path" path} :accept :json}
        result (http/get request-path request-params)
        json-result (http/json-decode (:body result))]
    (map #(get % "value") (get json-result "results"))))

(def possible-values
  "Fetch possible values using the clj-http library, as the imcljs implementations
  of possible-values and unique-values are broken"
  (memoize possible-values-))

(def summaries
  (memoize (partial fetch/summary-fields)))
