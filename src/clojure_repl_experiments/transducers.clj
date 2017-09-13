(ns clojure-repl-experiments.transducers)

;;; Tim Baldridge - Transducers - Episode 1 - Introduction to Transducers
;;; https://www.youtube.com/watch?v=WkHdqg_DBBs
(def data (vec (range 11)))

;; let's implement map easily with reduce
;; note, that this implementation is eager
(defn -map [f coll]
  (reduce
   (fn [acc v]
     (conj acc (f v)))
   []
   coll))
(-map inc data)

;; let's implement filter
(defn -filter [f coll]
  (reduce
   (fn [acc v]
     (if (f v)
       (conj acc v)
       acc))
   []
   coll))
(-filter odd? data)

;; let's compose!
(->> data
     (-map inc)
     (-filter odd?))

;; but there's some complecting here related to `reduce`
(defn -mapping [f]
  (fn [acc v]
    (conj acc (f v))))
(reduce (-mapping inc)
        []
        data)

;; Another complecting is `conj`
;; we're assuming that collection is "conjable"
;; let's add another level of function
(defn -mapping [f]
  (fn [xf]
    (fn [acc v]
      #_(println "mapping: " v " -> " (f v))
      (xf acc (f v)))))
(defn -filtering [f]
  (fn [xf]
    (fn [acc v]
      #_(println "filtering: " v " -> " (f v))
      (if (f v)
        (xf acc v)
        acc))))
;; "rfn" like "reducing function"
(def rfn (-mapping inc))
(def rfn (-filtering odd?))
;; let's compose mapping and filtering together
;; notice that -mapping if applied first, then -filtering
(def rfn (comp (-mapping inc) (-filtering odd?)))
(reduce (rfn conj)
        []
        data)

;; we can use other operations than `conj`
(reduce (rfn +)
        0
        data)

;;; Transducers episode 2
;;; https://www.youtube.com/watch?v=3E5dbAKIS_E
(def rfn (comp (-mapping int)
               (-mapping inc)
               (-filtering odd?)
               (-mapping char)))

(defn string-rf
  [^StringBuilder acc ^Character ch]
  (.append acc ch))

(str (reduce (rfn string-rf)
             (StringBuilder.)
             "Hello World"))
;; But now we're complecting construction of `StringBuilder`
;; with transformation `string-rf` and building the output with `str`
;; => Rich came with great idea to solve this problem with 3-arity function!
;; => Let's build a better string-rf
(defn string-rf
  ;; construction step
  ([] (StringBuilder.))
  ;; completing step
  ([^StringBuilder sb]
   (.toString sb))
  ;; transformation step
  ([^StringBuilder acc ^Character ch]
   (.append acc ch)))
;; we also need to fix `mapping` and `filtering`
(defn -mapping [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (xf acc (f v))))))
(defn -filtering [f]
  (fn [xf]
    (fn
      ([] (xf))
      ([acc] (xf acc))
      ([acc v]
       (if (f v)
         (xf acc v)
         acc)))))

(def xform (comp (-mapping int)
               (-mapping inc)
               (-filtering odd?)
               (-mapping char)))

(transduce xform string-rf "Hello World")

;; One cool thing that we can do with this stuff is to use transient vector!
(defn vec-trans
  ([] (transient []))
  ([acc] (persistent! acc))
  ([acc v] (conj! acc v)))
(transduce xform vec-trans "Hello World")



;;; Transducers - Episode 3 - IReduce
;;; https://www.youtube.com/watch?v=7Px1rk3qWGg

;; let's extend the CollReduce protocol
;;
;; This is what makes transducers really fast:
;; -> just function calls, no allocations of lazy seqs
(extend-protocol clojure.core.protocols/CollReduce
  java.io.InputStream
  (coll-reduce [this f init]
    (let [is ^java.io.InputStream this]
      (loop [acc init]
        (let [ch (.read is)]
          (if (= -1 ch)
            acc
            (recur (f acc ch))))))))
(transduce (map char)
           conj
           (java.io.ByteArrayInputStream. (.getBytes "Hello World")))

;; But, we should support early termination!
(extend-protocol clojure.core.protocols/CollReduce
  java.io.InputStream
  (coll-reduce [this f init]
    (let [is ^java.io.InputStream this]
      (loop [acc init]
        ;; notice reduced? usage
        (if (reduced? acc)
          @acc
          (let [ch (.read is)]
            (if (= -1 ch)
              acc
              (recur (f acc ch)))))))))
(transduce (comp (map char)
                 (map #(Character/toUpperCase %))
                 ;; testing early termination with `take`
                 ;; when you look at source code of `take` you'll see that it calls `reduced`
                 (take 3))
           conj
           (java.io.ByteArrayInputStream. (.getBytes "Hello World")))

;; Notes:
;; - Instead of extending protocol CollReduce we can also implement clojure.lang.IReduce - better performance?
;; - coll-reduce function also has 2-arity function (no initial value) - most of the time we shouldn't worry about it

