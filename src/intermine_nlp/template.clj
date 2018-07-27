(ns intermine-nlp.template
  (:require [imcljs.fetch :as im-fetch]
            [imcljs.path :as im-path]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.core.match :refer [match]]
            [clojure.walk :refer [walk]]
            [intermine-nlp.util :as util]))

(defn load-local-templates
  "Load a local copy of templates EDN file (backup for fetch-templates).
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse"
  [db-name]
  (let [path (str "test/queries/" db-name "_templates.edn")]
    (util/read-edn path)))


(defn fetch-templates-
  "Fetch templates for the given dataset. Defaults to flymine.
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse
  Accepts either a database name (fly, yeast...) or a service"
  [db]
  (match [db]
         [{:root root}]
         (try
           (im-fetch/templates {:root root})
           (catch java.lang.Exception e (print "Couldn't access database")))
         [db-name]
         (let [url (case db-name
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
                           :model {:name "genomic"}}]
           (try
             (im-fetch/templates db-request)
             (catch java.lang.Exception e (load-local-templates db-name))))))

(def fetch-templates
    "Fetch templates for the given dataset. Defaults to flymine.
  Options are:
  medic|fly|fly-b|human|yeast|rat|mouse
  Accepts either a database name (fly, yeast...) or a service"
  (memoize fetch-templates-))

(defn store-templates
  "Store templates map in the appropriate directory (test/queries/)."
  [templates path]
  (try (->> templates prn-str (spit path))
       (catch Exception e (printf "Couldn't store templates at '%s'." path))))

(defn get-template-paths
  "Returns a flattened list of all paths mentioned in constraints and views"
  [templates]
  (let [templates (vals templates)
        views (flatten (map #(get % :select) templates))
        constraints (walk (comp (partial map #(get % :path)) :where) identity templates)]
    (-> [views constraints] flatten distinct)))

(defn in-template?
  "Predicate: true if path exists in template, false otherwise."
  [service path]
  (let [model (:model service)
        templates (fetch-templates service)
        template-paths (get-template-paths templates)
        template-disp-names (flatten
                             (map
                              (partial im-path/display-name model)
                              template-paths))
        disp-name (im-path/display-name model path)]
    (every? #(some #{%} template-disp-names) disp-name)))

(defn gen-in-template?
  "Wrap in-template? with service, creating a continuation predicate function
  of 1 variable"
  [service]
  (partial in-template? service))
