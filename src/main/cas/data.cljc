(ns cas.data
  (:require [cas.utils :refer [key-gen]])
  #?(:cljs (:require-macros [cas.data :refer [atom->]]))
  )

(defn over
  ([f as]
   (over f as (key-gen)))
  ([f as k]
   (cond (vector? as)
         (let [arg-cache (atom (vec (map deref as)))
               r-atom (atom (apply f @arg-cache))]
           (add-watch arg-cache k (fn [k r o n] (when (not= o n)
                                                  (reset! r-atom (apply f n)))))
           (doseq [i (range (count as))]
             (add-watch (nth as i) (key-gen) (fn [k r o n]
                                               (swap! arg-cache assoc i n))))
           r-atom)
         :else
         (let [r-atom (atom (f @as))]
           (add-watch as k (fn [k r o n]
                             (reset! r-atom (f n))))
           r-atom))))

(defmacro atom-> [atm-form & clauses] ;;needs work---needs a cross-platform over
  (let [pairs (partition 2 clauses)
        reducer (fn [[assigns last-sym :as acc] [sym f]]
                  [(conj assigns `(def ~sym (over ~f ~last-sym)))
                   sym])]
    `(do
       ~@(first (reduce reducer [[] atm-form] pairs)))))
