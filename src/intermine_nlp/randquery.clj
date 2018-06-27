(ns intermine-nlp.randquery
  (:require [imcljs.query :as query]
            [imcljs.path :as im-path]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [intermine-nlp.util :as util]
            [intermine-nlp.template :as template]
            [imcljs.fetch :as im-fetch]
            [imcljs.path :as path]))


(defn rand-class
  "Returns the keyword of a random class in model."
  [model]
  (let [classes (-> model :classes (keys))]
    (rand-nth classes)))

(defn rand-val
  "Get a random value from a map"
  [m]
  (-> m vals rand-nth))

(defn rand-field
  "Given a model and path (seq of class keywords), return a random attribute."
  [model class-kws]
  (let [path (im-path/join-path class-kws)
        [& classes] class-kws]
    (if classes
      (try
        (rand-val (im-path/attributes model path))
        (catch Exception e nil))
      nil)))

(defn rand-path
  "Given a model and path (seq of class keywords), picks a random relation (child)
  and returns the resulting path of that relation."
  [model class-kws]
  (let [[& classes] class-kws]
    (if classes
      (try
        (vec
         (filter identity
                 (conj class-kws (->> class-kws
                                      im-path/join-path
                                      (im-path/relationships model)
                                      (remove #(some #{(first %)} class-kws))
                                      (into {})
                                      keys
                                      rand-nth))))
           (catch Exception e nil))
      [(rand-nth (-> model :classes keys ))]
      )))


(defn rand-attribute
  "Given a model and class keyword, randomly descend the tree of properties
   belonging to the class. When it has reached the specified depth, returns
  a random attribute. If it bottoms out before reaching depth, returns
  immediately."
  [model class-kws depth]
  (loop [current-kws class-kws height 0]
    (let [path (im-path/join-path current-kws)
          random-path (rand-path model current-kws)]
      (if
          (or
           (empty? random-path)
           (= height depth)) {:path current-kws :field (rand-field model current-kws)}
          (recur random-path (inc height) )
          ))))

(defn attr->path
  "Converts a path-attribute map to a string path.
  Example: (attr->path {:path [:Gene :primaryId] :field {:name 'blah'})
         => [:Gene :primaryId :blah]"
  [attribute-map]
  (let [class-kws (:path attribute-map)
        field-key (-> attribute-map :field :name keyword)]
    (try
      (im-path/join-path (conj class-kws field-key))
      (catch Exception e nil))))

(defn rand-possible-value
  "Return a random legal/possible value for a given path."
  [service path]
  (let [possible-values (util/possible-values service path)]
    (rand-nth possible-values)))


;;; other ops: "ONE_OF" "DOES_NOT_CONTAIN" "LOOKUP" "EXACT_MATCH"
;;;  "MATCH" "DOES_NOT_MATCH"    "IS_NULL"
(def string-ops ["=" "!=" "<" ">" "<=" ">=" "CONTAINS" "LIKE"])
(def int-ops ["=" "!=" "<" ">" "<=" ">="])
(def bool-ops ["=" "!="])

(defn rand-op
  "Returns a random operation of given type and arity (single vs multiple)."
  [type]
  (case type
    "java.lang.String" (rand-nth string-ops)
    "java.lang.Integer" (rand-nth int-ops)
    "int" (rand-nth int-ops)
    "java.lang.Double" (rand-nth int-ops)
    "java.lang.Float" (rand-nth int-ops)
    "java.lang.Boolean" (rand-nth bool-ops)
    "org.intermine.objectstore.query.ClobAccess" nil
    ))

(defn rand-value
  "Generate a random value of a given type (doesn't necessarily occur in database)."
  [type]
  (str (case type
         "java.lang.String" (apply str (take 10 (repeatedly #(char (+ (rand 26) 65)))))
         "java.lang.Integer" (rand-int 10000)
         "int" (rand-int 10000)
         "java.lang.Double" (rand 100)
         "java.lang.Float" (rand 100)
         "java.lang.Boolean" (rand-nth [true false])
         "org.intermine.objectstore.query.ClobAccess" nil
         )))

(defn rand-constraint
  "Selects a random attribute of given depth at the given path,
  as well as a compatible operation and value. Returns a map containing
  :path :op :value."
  [service class-kws depth]
  (let [model (:model service)
        ;; pred (or pred constantly)
        attributes (repeatedly #(rand-attribute model class-kws depth))
        attr (first (filter #(template/in-template? service (attr->path %)) attributes))
        path (attr->path attr)
        field-type (get-in attr [:field :type])]
    {:path path
     :op (rand-op field-type)
     :value (rand-value field-type)}))

(defn rand-depth-constraint
  "Returns a random constraint with depth between 1 and 'depth'.
  Default is flatly distributed, with optional weight vector for
  specifying a probability distribution. class-kws specifies path."
  ([service class-kws max-depth]
   (let [depth (inc (rand-int max-depth))]
     (rand-constraint service class-kws depth)))
  ([service class-kws max-depth probs]
   (let [weighted-vec (flatten (map repeat probs max-depth))
         depth (rand-nth weighted-vec)]
     (rand-constraint service class-kws depth))))

(defn rand-view
  "Selects n random views (paths) of given depth."
  [service class-kws num-views depth]
  (let [model (:model service)
        attributes (repeatedly #(rand-attribute model class-kws depth))
        attrs (take num-views (filter #(template/in-template? service (attr->path %)) attributes))]
    (vec (map (partial attr->path) attrs))))

(defn rand-summary-view
  "Like rand-view, but returns a summary view for a relation (class) rather than a
  field (attribute)."
  [service class-kws depth]
  (letfn [(merge-paths [path summaries]
            (string/join "." (-> path
                                 (string/split #"\.")
                                 drop-last
                                 (concat (string/split summaries #"\."))
                                 )))]
    (let [model (:model service)
          class-kws (:path (rand-attribute model class-kws (dec depth)))
          path (im-path/join-path class-kws)
          top-path (im-path/adjust-path-to-last-class model path)
          top-path-kw (keyword top-path)
          summary (top-path-kw (util/summaries service))]
      (vec (map (partial merge-paths path) summary)))))


(defn rand-query
  "Returns a random constraint with depth between 1 and 'depth'.
  Default is flatly distributed, with optional weight vector for
  specifying a probability distribution. class-kws specifies path."
  [service num-views num-summaries num-constraints]
  (let [view-max-depth 3
        constraint-max-depth 3
        model (:model service)
        view-depth (inc (rand-int view-max-depth))
        constraint-depth (inc (rand-int constraint-max-depth))
        class-kw [(rand-class model)]]
    {:select (vec (flatten (concat
                            (rand-view service class-kw num-views view-depth)
                            (repeatedly num-summaries (partial rand-summary-view service class-kw view-depth)))))
     :where (vec (repeatedly num-constraints
                         (partial rand-depth-constraint service class-kw constraint-depth)))}))
