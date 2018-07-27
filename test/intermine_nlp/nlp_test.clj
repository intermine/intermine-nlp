(ns intermine-nlp.nlp-test
  (:require [clojure.test :refer :all]
            [intermine-nlp.nlp :as nlp]
            [intermine-nlp.model :as model]))

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

(deftest tokenize-test
  (testing "Testing tokenizer"
    (are [x y] (= x y)
      (nlp/tokenize "This is a sentence") ["This" "is" "a" "sentence"]
      (nlp/tokenize "These are words. Jake's words?") ["These" "are" "words" "." "Jake" "words" "?"])))

(deftest lemmatize-text-test
  (testing "Testing basic lemmatizer AND lemmatize-as-text"
    (are [x y] (= (nlp/lemmatize-as-text  x) y)
      "CapitaliZation" "capitalization"
      "Somebody's possessive s" "somebody possessive s"
      "plurals" "plural"
      "are infinitives working?" "be infinitive work ?")))

;;; Non-trivial to test nlp/lemma-map properly (lemmatization is context-dependant)
(deftest lemma-map-test
  (let [lemma-map (nlp/lemma-map "Here is a trivial testing sentence for your enjoyment!")]
    (testing "Testing lemma-map function"
      (is
       (every? (fn [[k v]] (= (nlp/lemmatize-as-text k) v)) lemma-map)))))
