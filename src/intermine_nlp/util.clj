(ns intermine-nlp.util
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [imcljs.fetch :as im-fetch]
            [imcljs.path :as im-path]
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
  (let [split-path (split (im-path/adjust-path-to-last-class (:model service) path) #"\.")
        query {:from (first split-path)
               :select [(last split-path)]}
        rows (try (im-fetch/rows service query)
                  (catch java.lang.Exception e nil))]
       (->> rows :body :results (map first) distinct)))

(def pseudo-possible-values
    "imcljs.fetch/possible-values seems to be broken, so here's an implementation
  using only imcljs.fetch/rows. 'path' must point to a field, not a class."
  (memoize pseudo-possible-values-))

(defn possible-values-
  "Fetch possible values using the clj-http library, as the imcljs implementations
  of possible-values and unique-values are broken"
  [service path]
  (let [request-path (str "http://" (:root service) "/service/im-path/values")
        request-params {:query-params {"path" path} :accept :json}
        result (http/get request-path request-params)
        json-result (http/json-decode (:body result))]
    (map #(get % "value") (get json-result "results"))))

(def possible-values
  "Fetch possible values using the clj-http library, as the imcljs implementations
  of possible-values and unique-values are broken"
  (memoize possible-values-))

(def fetch-summaries
  "Fetch summary fields for all classes in a database.
  Argument: service"
  (memoize (partial im-fetch/summary-fields)))

(defn class-summary
  "Fetches the summary for a given class from a given db service"
  [service class-name]
  (let [summaries (fetch-summaries service)]
    (get summaries (keyword class-name))))

(defn un-lemmatize
  "Convert a lemmatized  path back to its schema form. If no match (or if
  the path is already in schema form), return the original path."
  [path lemma-path-map]
  (or (get lemma-path-map path) path))

(defn class-names
  "Returns a list of class names (strings) for all classes in a given model."
  [model]
  (->> model
       :classes
       keys
       (map name)
       distinct))

(defn field-names
  "Returns a list of field names (strings) for all classes in a given model.
  If class provided, returns list of fields for given class in model."
  ([model]
   (let [class-paths (class-names model)]
     (distinct (flatten (map #(->> %
                                   (im-path/attributes model)
                                   keys
                                   (map name)) class-paths)))))
  ([model class]
   (distinct (flatten (->> class
                           (im-path/attributes model)
                           keys
                           (map name))))))
