(ns intermine-nlp.query-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.model :as model]
            [intermine-nlp.query :as query]))

(def db-model (model/fetch-model "fly"))
(def service {:root "www.flymine.org/query"
              :model db-model})

(deftest gen-query-test
  (let [tree1 {:VIEW [{:PATH {:CLASS "gene"}}], :CONSTRS [{:CONSTR {:PATH {:CLASS "chromosome", :FIELD "length"}, :COMPARE :LT, :VALUE "10000"}}]}
        tree2 {:VIEW [{:PATH {:CLASS "chromosome"}}], :CONSTRS [{:CONSTR {:PATH {:FIELD "identifier"}, :VALUE "foo"}}]}
        tree3 {:VIEW [{:PATH {:CLASS "protein"}}], :CONSTRS [{:CONSTR {:PATH {:FIELD "length"}, :COMPARE :LT, :VALUE "50000"}}]}]
    (testing "Testing gen-query method"
      (are [tree generated-query] (= generated-query (query/gen-query service tree))
        tree1 {:from "Gene", :select ["Gene.secondaryIdentifier" "Gene.symbol" "Gene.primaryIdentifier" "Gene.organism.name"], :where [{:path "chromosome.length", :op "<", :value "10000"}]}
        tree2 {:from "Chromosome", :select ["Chromosome.primaryIdentifier" "Chromosome.organism.name" "Chromosome.length"], :where [{:path "Chromosome.primaryIdentifier", :op "=", :value "foo"}]}
        tree3 {:from "Protein", :select ["Protein.primaryIdentifier" "Protein.primaryAccession" "Protein.organism.name"], :where [{:path "Protein.length", :op "<", :value "50000"}]}
        ))))
