(ns intermine-nlp.core
  (:require [intermine-nlp.parse :as parse]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [clojure.string :as string]
            [clojure.pprint :refer [pprint]]
            [clojure.set :refer [map-invert]]
            [imcljs.query :as im-query]
            [intermine-nlp.query :as query]
            [intermine-nlp.fuzzy :as fuzzy])
  (:gen-class))

(def default-service {:root "www.flymine.org/query" :model {:name "genomic"}})
(def default-model (model/fetch-model default-service))

(defn parser-pipeline
  "Generate a parser pipeline for a given InterMine model.
  service should be of the format {:root \"url-of-db-query-service\"}.
  options: :debug (prints / visualizes at each step)
  "
  [service & {:as options}]
  (let [model (try (model/fetch-model service)
                   (catch Exception e (model/fetch-model (assoc service :model {:name "genomic"}))))
        service (assoc service :model model)
        parser (parse/gen-parser model)
        threshold (or (:threshold options) 0.8)]
    (cond
      (:debug options)
      #(as-> % $
         (nlp/lemmatize-as-text $)
         (doto $ println)
         (fuzzy/replace-class-names model threshold $)
         (doto $ println)
         (fuzzy/replace-field-names model threshold $)
         (doto $ println)
         (nlp/lemmatize-as-text $)
         (doto $ println)
         (insta/parse parser $)
         (doto $ insta/visualize)
         (parse/transform-tree $)
         (doto $ println)
         (query/gen-query service $)
         )
      :else
      #(->> %
         nlp/lemmatize-as-text
         (fuzzy/replace-class-names model threshold)
         (fuzzy/replace-field-names model threshold)
         nlp/lemmatize-as-text
         (insta/parse parser)
         parse/transform-tree
         (query/gen-query service)
         ))))

(defn -main
  "I'll try to parse your English query and give you the PathQuery translation."
  [& args]
  (let [fly-model (model/fetch-model "fly")
        fly-service {:root "www.flymine.org/query" :model fly-model}
        pipeline (parser-pipeline fly-service)]
    (pprint "Enter a simple query and I'll attempt to parse it!")
    (pprint "Example: Show me genes with primaryIdentifier like ovo.")
    (loop []
      (print "\n")
      (pprint "To quit, hit <ENTER>")
      (let [text (read-line)
            result (pipeline text)]
        (if (not (empty? text))
          (do (cond
                (every? empty? result) (pprint "Sorry, I couldn't parse that.")
                :else                   (pprint result))
              (recur))
          (pprint "Bye, and happy hacking."))))))
