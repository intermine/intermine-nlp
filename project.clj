(defproject intermine-nlp "0.1.1-SNAPSHOT"
  :description "intermine-nlp provides translation from natural (English) language to PathQuery queries."
  :url "http://example.com/FIXME"
  :license {:name "Gnu Lesser General Public License Version 2.1"
            :url "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html"}
  :java-source-paths ["java"]
  :resource-paths ["resources" "resources/nlp_models" "resources/db_models"]
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; [org.intermine/imcljs "0.5.1"]
                 [org.intermine/imcljs "0.6.0-SNAPSHOT"]
                 [clj-http "3.9.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/core.match "0.3.0-alpha5"]
                 [clojure-opennlp "0.4.0"]
                 ;; [org.clojurenlp/core "3.7.0"]
                 ;; [com.zensols.nlp/parse "0.1.4"]
                 [edu.stanford.nlp/stanford-corenlp "3.9.1"]
                 [edu.stanford.nlp/stanford-corenlp "3.9.1" :classifier "models"]
                 [edu.stanford.nlp/stanford-parser "3.9.1"]
                 [edu.stanford.nlp/stanford-parser "3.9.1" :classifier "models"]
                 [instaparse "1.4.9"]
                 [rhizome "0.2.9"]
                 [clj-fuzzy "0.4.1"]
                 [org.clojure/math.combinatorics "0.1.4"]
                 ]
  :main intermine-nlp.core
  ;; :main ^:skip-aot intermine-nlp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
