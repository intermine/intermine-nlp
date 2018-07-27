(ns intermine-nlp.parse-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.parse :as parse]
            [intermine-nlp.model :as model]
            [intermine-nlp.randquery :as randq]
            [intermine-nlp.util :as util]
            [instaparse.core :as insta]
            [imcljs.path :as im-path]
            [clojure.test :as test]))

(def db-model (model/fetch-model "fly"))
(def service {:root "www.flymine.org/query"
              :model db-model})

(def sample-text "Man request adapted spirits set pressed. Up to denoting subjects sensible feelings it indulged directly. We dwelling elegance do shutters appetite yourself diverted. Our next drew much you with rank. Tore many held age hold rose than our. She literature sentiments any contrasted. Set aware joy sense young now tears china shy.
To shewing another demands to. Marianne property cheerful informed at striking at. Clothes parlors however by cottage on. In views it or meant drift to. Be concern parlors settled or do shyness address. Remainder northward performed out for moonlight. Yet late add name was rent park from rich. He always do do former he highly.
Savings her pleased are several started females met. Short her not among being any. Thing of judge fruit charm views do. Miles Mr. an forty along as he. She education get middleton day agreement performed preserved unwilling. Do however as pleased offence outward beloved by present. By outward neither he so covered amiable greater. Juvenile proposal betrayed he an informed weddings followed. Precaution day see imprudence sympathize principles. At full leaf give quit to in they up.
Is post each that just leaf no. He connection interested so we an sympathize advantages. To said is it shed want do. Occasional middletons everything so to. Have spot part for his quit may. Enable it is square my an regard. Often merit stuff first oh up hills as he. Servants contempt as although addition dashwood is procured. Interest in yourself an do of numerous feelings cheerful confined.
At every tiled on ye defer do. No attention suspected oh difficult. Fond his say old meet cold find come whom. The sir park sake bred. Wonder matter now can estate esteem assure fat roused. Am performed on existence as discourse is. Pleasure friendly at marriage blessing or.
Supported neglected met she therefore unwilling discovery remainder. Way sentiments two indulgence uncommonly own. Diminution to frequently sentiments he connection continuing indulgence. An my exquisite conveying up defective. Shameless see the tolerably how continued. She enable men twenty elinor points appear. Whose merry ten yet was men seven ought balls.
How promotion excellent curiosity yet attempted happiness. Gay prosperous impression had conviction. For every delay death ask style. Me mean able my by in they. Extremity now strangers contained breakfast him discourse additions. Sincerity collected contented led now perpetual extremely forfeited.
From they fine john he give of rich he. They age and draw Mrs. like. Improving end distrusts may instantly was household applauded incommode. Why kept very ever home Mrs. Considered sympathize ten uncommonly occasional assistance sufficient not. Letter of on become he tended active enable to. Vicinity relation sensible sociable surprise screened no up as.
Put all speaking her delicate recurred possible. Set indulgence inquietude discretion insensible bed why announcing. Middleton fat two satisfied additions. So continued he or commanded household smallness delivered. Door poor on do walk in half. Roof his head the what.
On no twenty spring of in esteem spirit likely estate. Continue new you declared differed learning bringing honoured. At mean mind so upon they rent am walk. Shortly am waiting inhabit smiling he chiefly of in. Lain tore time gone him his dear sure. Fat decisively estimating affronting assistance not. Resolve pursuit regular so calling me. West he plan girl been my then up no.")


(deftest model-grammar-test
  (let [model-class-parser (insta/parser (parse/model-parser db-model) :start :CLASS)
        model-field-parser (insta/parser (parse/model-parser db-model) :start :FIELD)
        class-names (->> db-model :classes vals (map :name))
        fields (->> (map keyword class-names)
                    (map (fn [class-kw] (randq/rand-field db-model [class-kw])))
                    (repeat 2)
                    distinct
                    flatten)
        field-names (distinct (map :name fields))]
    (testing "Testing model-parser"
      (are [classes] (every? #(= (insta/parse model-class-parser %) [:CLASS %]) classes)
        class-names)
      (are [fields] (every? #(= (insta/parse model-field-parser %) [:FIELD %]) fields)
        field-names))))

(deftest gen-parser-test
  ;; TODO: parse collection of human-generated queries
  (let [parser1 (parse/gen-parser db-model)]
    (testing "testing gen-parser function"
      (are [string] (insta/failure? (insta/parse parser1 string))
        "This sentence does not parse."
        "which Genes doesn't parse."
        "Gene length for all")
      (are [string] ((complement insta/failure?) (insta/parse parser1 string))
        "show me Gene with primaryIdentifier ovo"))))
