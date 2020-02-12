(ns cas.rein-til
  (:require [clojure.walk :as walk]
            [rum.core :as rum]))



                                        ;see http://dirk.jivas.de/papers/buchheim02improving.pdf
;and https://rachel53461.wordpress.com/tag/reingold-tilford/
(def steps  '(("Do a post-order traversal of the tree"
                
                "Assign an X value to each node of 0 if it’s a left-most node, or leftSibling.X + 1 if it’s not."


                "For each parent node, we want the node centered over the children. This would be the midway point between the first child’s X position, and the last child’s X position.
 If the parent has no left sibling, change it’s X value to this midpoint value. If it has a left sibling, we’re going to store it in another node property. I’m calling this property Mod just because that’s what I see it called in other examples.

    The Mod property is used to determine how much to modify the children’s X values in order to center them under the parent node, and will be used when we’re done with all our calculates to determine the final X value of each node. It should actually be set to Parent.X – MiddleOfChildrenX to determine the correct amount to shift the children by.
"


                "    Check that this tree does not conflict with any of the previous sibling trees, and adjust the Mod property if needed. This means looping through each Y level in the current node, and checking that the right-most X value of any sibling to the left of the node does not cross the left-most X value of any child in the current node.
")

              ("Do a second walk through the tree to determine that no children will be drawn off-screen, and adjust the Mod property if needed. This can happen when if the Mod property is negative.")

              ("Do a third walk through the tree to determine the final X values for each node. This will be the X of the node, plus the sum of all the Mod values of all parent nodes to that node.
")))




(def ftree
  (->> ["=" ["+" 2 3 4] ["-" 999  ["+" 992 2]]]
       (walk/postwalk (fn valify [item]
                        (if (vector? item) item
                            {:val item})))
       ((fn assign-ys [level tree]
          (apply vector (assoc (first tree) :y level)
                 (map #(cond (vector? %)
                              (assign-ys (inc level) %)

                              (map? %)
                              (assoc % :y (inc level))) (rest tree)))) 0)))


(def children rest)

(def midpoint #(/ (+ % %2) 2))

(defn gv [n]
  (cond (vector? n) (first n)
         :else n))

(def has-children? vector?)

(defn initial-mod [n]           ;of parents that aren't first siblings
  )

(defn set-child-local-xs [[h & t]] (into [h]
                                         (map-indexed
                                          (fn [i item]
                                            (cond (vector? item)
                                                  (assoc-in item [0 :x] i)
                                        ;we want to set this to midpoint of the rest 
                                        ;but only if no left siblings
                                        ;hence what we actually want to do
                                        ;is do that for first children
                                        ;for later ones we want to assign mod= currentval - midpoint of (its) children
                                                  (map? item) ;if a compound map structure this will be trouble
                                                  (assoc item :x i)

                                                  :else item
                                                  ))
                                          t)))





(defn rein-til
  "given a simple vector tree, returns a tree of that same form but with x vals
  a 'simple vector tree' is lisp code, but in vecs.  the first item of a vector is the root, everything after is children.  Note that the first item is NEVER a vector!  Examples:
  
  [1 [2 3]]    [1 2 [3 4] 5] 

   1               1 
  / \\            /|\\ 
  2   3          2 3 5
                    |
                    4

  The fns first, rest, and next might be useful (rest might return '(), next might return nil)
  "

  [tree]
  (let [first-walk-result (walk/postwalk (fn [sf] ;head tail
                                           (cond (vector? sf)
                                                 (set-child-local-xs sf)
                                                 
                                                 :else
                                                 sf))
                                         
                                         (assoc-in tree [0 :x] 0))]
    first-walk-result))




(defn mult-coords [tree]
  (let [f #(* % 100)]
    (walk/postwalk (fn [item]
                     (if (map? item)
                       (-> item
                           (update :x f)
                           (update :y f))
                       item)) tree)))


(rum/defc node-comp < rum/reactive [{:keys [x y] :as node}]
  [:g {:transform (str "translate(" x "," y ")")
       :on-click #(js/alert "hi")}
   [:text {:style {:stroke "black"
                   :font "italic"
                   :size "8"
                   :x 10
                   :y 50}}
    (str (:val node))
    #_(str "x:" x ", y:" y ", val:" (:val node))]
   [:rect {:style {}
           :stroke "black"
           :stroke-width "2"
           :width 50 
           :height 50
           :fill "white"}]])

(def datm (atom nil))

#_(rum/defc node-tree [ntree]
  (let [arranged (-> ntree rein-til mult-coords)
         _ (println arranged)
    ;task is to build a list from the recursive form
        flat (list-from-tree arranged)]
    (apply array (for [i flat]
                     (dumb-comp i)))))


;component to drop on screen
#_[:svg {:style {:clear "both"}
         :width "100%"
         :height "950px"}
   (node-tree r-t/ftree)
   ]

;SO answer on good way to make react components draggable: https://stackoverflow.com/questions/20926551/recommended-way-of-making-react-component-div-draggable

;React hooks:https://reactjs.org/docs/hooks-overview.html


(defn list-from-tree [tree]
  (let [root (first tree)
        children (rest tree)]
    (concat [root] (mapcat #(if (vector? %)
                              (list-from-tree %)
                              [%])
                           children))))
