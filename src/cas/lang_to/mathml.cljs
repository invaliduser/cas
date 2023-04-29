(ns cas.lang-to.mathml
  (:require [rum.core :as rum]
            [cas.state :refer [highlight-atom tree-atom]]
            [cas.tree-ops :refer [real-path children children? represents-fn? remove-last doto-last node-val nodal-descendant logical-descendant vassoc]]))

(def non-parent-operators
  {:+ "+"
   :- "-"
   :* "∙"
   :dby "÷"
   := "="})

(defn operator? [thing]
  (non-parent-operators thing))

(defn add-attr [node k v]
  (cond (map? (second node))
        (assoc-in node [1 k] v)
        :else (apply vector (first node) {k v} (rest node))))

(def fns
  (let [frac-f (fn [[f s] path] [:mfrac f s])]
    {
     :/  frac-f
     :frac frac-f
     :ov frac-f
     :paren (fn [content path]
              [:mrow
               [:mo "("]
               content
               [:mo ")"]])
     := (fn [[f s] path]
          [:mrow f [:mo "="] s]) ;change to so =:+::equation:sum

     :sum (fn [items path]
            (into [:mrow] items))
     :product (fn [items path]
                (into [:mrow] items))
     :term (fn [items path]
             (into [:mrow] items))
     
     :exp   (fn [[base exp] path] [:msup base exp])
     #_#_:prime (fn [item] (str item "^{\\prime}"))
}))

(defn path-stuff [node path]
  (-> node
   (add-attr :on-click
             #_(reset! highlight-atom path)
             #(println path))
   (add-attr :style {:background-color
                     (if (or (nodal-descendant (rum/react highlight-atom) path))
                       "green"
                       "red")})))

(declare render-item)
(rum/defc parent-op-fn < rum/reactive [[kw & args] path]
  (let [f (fns kw)
        children (map-indexed (fn [idx item]
                                (let [new-path  (conj path (inc idx))]
                                  (rum/with-key (render-item item new-path) new-path))) ;meh
                              (filter identity args))
        node (f children path)]
    (path-stuff node path)))

(rum/defc mo < rum/reactive [item path]
  (-> [:mo (non-parent-operators item)]
      (path-stuff path)))

(rum/defc mn < rum/reactive [item path]
  (->  [:mn item]
       (path-stuff path)))

(rum/defc mi < rum/reactive [item path]
  (-> [:mi item]
      (path-stuff path)))

#_#_:on-click #(reset! highlight-atom path)
#_(if (rum/react show-paths?) (str "|" path))
#_(if (or (nodal-descendant (rum/react highlight-atom) path))
    [:mark content]
    content)

"need a fn that takes ...something, and adds the highlighting, etc"

#_(let [marked? (nodal-descendant (rum/react highlight-atom) path)
        select! #(reset! highlight-atom path)])

(defn render-item [item path]
  (cond (vector? item)
        (parent-op-fn item path)

        (operator? item)
        (mo item path)

        (number? item)
        (mn item path)

        (string? item)
        (mi item path)

        (nil? item)
        (do (println "probably shouldn't be nil...here is the tree: " @tree-atom))))

(defn render-to-mathml [manipulang]
  [:math
   (render-item manipulang [0])])
