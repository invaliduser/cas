(ns cas.lang-to.mathml
  (:require [rum.core :as rum]
            [cas.frontend.state :refer [highlight-atom tree-atom] :as state]
            [cas.tree-ops :refer [real-path children children? represents-fn? remove-last doto-last node-val nodal-descendant logical-descendant vassoc]]))

(def non-parent-operators
  {:+ "+"
   :- "-"
   :* "∙"
   :dby "÷"
   := "="})

(defn operator? [thing]
  (non-parent-operators thing))



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

     :list (fn [items path]
             (into [:mrow] items))
     :sum (fn [items path]
            (into [:mrow] items))
     :product (fn [items path]
                (into [:mrow] items))
     :term (fn [items path]
             (into [:mrow] items))
     
     :exp   (fn [[base exp] path] [:msup base exp])
     #_#_:prime (fn [item] (str item "^{\\prime}"))
}))




#_(defn path-stuff [node path]
  (reduce (fn [n f]
            (f n path))
          node
          *transforms*))



#_(defn each-node [node path]
    (-> node
        (add-attr :on-click
                  #_(reset! highlight-atom path)
                  #(println path))
        (add-attr :style {:background-color
                          (if (or (nodal-descendant (rum/react highlight-atom) path))
                            "green"
                            "red")})))

(rum/defcontext *context*)
(declare render-item)
(rum/defc parent-op-fn < rum/reactive [[kw & args] path each-node]

  (let [f (fns kw)
        children (map-indexed (fn [idx item]
                                (let [new-path  (conj path (inc idx))]
                                  (rum/with-key (render-item item new-path each-node) new-path))) ;meh
                              (filter identity args))
        node (f children path)]
    (each-node node path)))

(rum/defc mo < rum/reactive [item path each-node]
  (-> [:mo (non-parent-operators item)]
      (each-node path)))

(rum/defc mn < rum/reactive [item path each-node]
  (->  [:mn item]
       (each-node path)))

(rum/defc mi < rum/reactive [item path each-node]
  (-> [:mi item]
      (each-node path)))

#_#_:on-click #(reset! highlight-atom path)
#_(if (rum/react show-paths?) (str "|" path))
#_(if (or (nodal-descendant (rum/react highlight-atom) path))
    [:mark content]
    content)

"need a fn that takes ...something, and adds the highlighting, etc"

#_(let [marked? (nodal-descendant (rum/react highlight-atom) path)
        select! #(reset! highlight-atom path)])

(defn render-item [item path each-node]
  (cond (vector? item)
        (parent-op-fn item path each-node)

        (operator? item)
        (mo item path each-node)

        (number? item)
        (mn item path each-node)

        (string? item)
        (mi item path each-node)

        (nil? item)
        (do (println "probably shouldn't be nil...here is the tree: " @tree-atom))))

(defn add-attr [node k v]
  (cond (map? (second node))
        (assoc-in node [1 k] v)
        :else (apply vector (first node) {k v} (rest node))))

(defn add-click-to-set-path [node path]
  (add-attr node :on-click #(reset! highlight-atom path)))

(defn add-click-to-print-path [node path]
  (add-attr node :on-click #(print path)))

(defn add-select-highlight [node path]
  (add-attr node :style {:background-color
                         (if (or (nodal-descendant (rum/react highlight-atom) path))
                           "green"
                           "red")}))


(rum/defc render-to-navigable-mathml [manipulang]
  [:math
   (render-item manipulang [0] (fn [node path] (-> node
                                                   (add-select-highlight path)
                                                   (add-click-to-set-path path)) ))])

(rum/defc render-to-inert-mathml [manipulang]
  [:math (render-item manipulang [0] (fn [node path] node))])
