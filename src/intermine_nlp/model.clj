(ns intermine-nlp.model
  (:require [imcljs.fetch :as im-fetch]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.core.match :refer [match]]))

(defn load-local-model
  "Load a local copy of a model EDN file (backup for fetch-model).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (try (with-open [file (io/reader path)]
           (-> file java.io.PushbackReader. edn/read))
         (catch Exception e
           (printf "Couldn't open local model '%s'." path)
           ))))

(defn fetch-model
  "Fetch a model for the given dataset. Defaults to flymine.
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse
  Can optionally supply a database service ({:root XXX}) instead.
  Optional keyword argument ':no-fallback true' to prevent falling back to local store."
  [db & {:as options}]
  (match [db]
         [{:root root}]
         (try
           (im-fetch/model {:root root})
           (catch java.lang.Exception e (println "Couldn't access remote database")))
         [db-name]
         (let [url (case db-name
                     "medic"    "medicmine.jcvi.org/medicmine"
                     "fly"      "www.flymine.org/query"
                     "human"    "www.humanmine.org/humanmine"
                     "yeast"    "yeastmine.yeastgenome.org/yeastmine"
                     "rat"      "ratmine.mcw.edu/ratmine"
                     "mouse"    "www.mousemine.org/mousemine"
                     "fly-beta" "beta.flymine.org/beta"
                     java.lang.Exception)
               db-request {:root url
                           :token nil
                           :model {:name "genomic"}}]
           (try
             (im-fetch/model db-request)
             (catch java.lang.Exception e
               (if (:no-fallback options)
                   (println "Couldn't access remote database")
                   (do
                     (println "Couldn't access remote database, attempting local read")
                     (load-local-model db-name)
                     )))))))

(defn store-model
  "Store a model in the appropriate directory (resources/db_models)."
  [model db-name]
  (let [path (str "resources/db_models/" db-name "mine_model.edn")]
    (binding [*print-length* false]
      (try (->> model pr-str (spit path))
           (catch Exception e (printf "Couldn't store model at '%s'." path))))))

