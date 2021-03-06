(ns cas.lang-to-nodes
  (:require [cas.comps.board :as board]))

(defn create-node
  ([v path]
   (create-node v path []))
  ([v path ch]
   {::v v ::path path ::ch ch}))



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

