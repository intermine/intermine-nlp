(ns intermine-nlp.model
  (:require [imcljs.fetch :as fetch]
            [imcljs.path :as path]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn load-local-model
  "Load a local copy of a model EDN file (backup for fetch-model).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (try (with-open [file (io/reader path)]
           (-> file java.io.PushbackReader. edn/read))
         (catch Exception e (printf "Couldn't open local model '%s'." path)))))

(defn fetch-model
  "Fetch a model for the given dataset. Defaults to flymine.
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse
  Can optionally supply a database service ({:root XXX}) instead."
  ([database]
   (cond
     (string? database)
     (let [url (case database
                 "medic"    "medicmine.jcvi.org/medicmine"
                 "fly"      "www.flymine.org/query"
                 "human"    "www.humanmine.org/humanmine"
                 "yeast"    "yeastmine.yeastgenome.org/yeastmine"
                 "rat"      "ratmine.mcw.edu/ratmine"
                 "mouse"    "www.mousemine.org/mousemine"
                 "fly-beta" "beta.flymine.org/beta"
                 "www.flymine.org/query")
           db-request {:root url
                       :token nil
                       :model "genomic"}]
       (try
         (fetch/model db-request)
         (catch java.lang.Exception e (load-local-model database))))

     (map? database)
     (let [db-request {:root (:root database)
                       :token nil
                       :model "genomic"}]
       (try
         (fetch/model db-request)
         (catch java.lang.Exception e nil))))))


(defn store-model
  "Store a model in the appropriate directory (resources/db_models)."
  [model db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (try (->> model prn-str (spit path))
         (catch Exception e (printf "Couldn't store model at '%s'." path)))))
