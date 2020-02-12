(ns cas.lang-to-nodes
  (:require [cas.comps.board :as board]))

(defn create-node
  ([v path]
   (create-node v path []))
  ([v path ch]
   {::v v ::path path ::ch ch}))

(defn gen-fn-for-commutative-infix [op]
  (fn [& operands]
    (let [total-terms  (-> operands count (* 2) dec)]
      (apply str (take total-terms (interleave operands (repeat op)) )))))

(def fns {:+ (gen-fn-for-commutative-infix "+")
          :* (gen-fn-for-commutative-infix "*")
          :- (fn [f s]
               (str f "-" s))
          :/  (fn [f s]
               (str f "/" s))
          
          :frac (fn [f s]
                  (str "\\frac{" f "}{" s "}"))})

(defn compile-to-nodes
  [form path]
  (println "working")
  (cond (vector? form)
        (let [paths (take (count form) (map #(do
                                               (println "path:" path)
                                               (conj path %)) (range)))]
          (println "paths: " paths)
          (println "forms: " form)
          #_(board/create-node (first form)
                             path
                             (mapv compile-to-nodes
                                   paths
                                   (rest form))))

        (number? form)
        (create-node form path)

        (keyword? form)
        (create-node form path))) 

