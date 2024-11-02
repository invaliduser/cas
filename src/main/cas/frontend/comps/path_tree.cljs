(ns cas.comps.path-tree
  (:require [rum.core :as rum :refer-macros [defc]]
            [cas.frontend.tex-render :refer [render-tex]]
            [cljs.core.async :refer [chan <! >! go-loop]]
            [cas.frontend.state :refer [tree-atom highlight-atom show-paths? curr-value parent-path] :as state]
            [cas.frontend.tree-ops :refer [real-path children children? delete-at represents-fn? remove-last doto-last node-val nodal-descendant logical-descendant replace-last vassoc vget-in remove-range remove-at-index vremove vinsert vsplice] :as tree-ops]


            [cas.shorthand :as sh]))

(defn matches-real-path? [np hp] ;[node-path highlight-path]; both are real-path
  (or (= np hp)  ;is highlighted directly and specifically
   (nodal-descendant hp np)  ;ancestor is highlighted
   ))

(defn matches-path? [node-path highlight-path]
  (let [cn (count node-path)
        ch (count highlight-path)] ;move highlight-path out so not calculated in render
    (or
     ;is higlighted
     (and (= cn ch)
          (= node-path highlight-path))
                                        ;parent is highlighted
     (and (> cn ch)
          (= (subvec node-path 0 ch) highlight-path))
                                        ;parent is not highlighted but has :children
     (and (= cn ch)
          (= (subvec node-path 0 (dec cn))
             (subvec highlight-path 0 (dec cn)))
          (= (last highlight-path) :children))
                                        ;:all
     (= node-path [:all]))))

(defc node-comp < rum/reactive [node path]
  ;current node-comp doesn't worry about children or even the node, neither will we
  #_[:div
   [:span "node: " (str node) ", "]
   [:span "path: " (str path)]
   [:span "path result: " (str (vget-in @tree-atom path ))]
   [:span ", worked: " (str (= (vget-in @tree-atom path) node))]
   [:span ", logical-descendant? " (str (logical-descendant @highlight-atom path))]]
  (let [content (str node)]
    [:span {:style {:margin-left (-> (count path)
                                     (+ (if (= (last path) 0) 0 1))
                                     (* 10))}
              :on-click #(reset! highlight-atom path)}
       (if (or
            ;need one for exact match, e.g. way to highlight a =
            (nodal-descendant (rum/react highlight-atom) path))
         [:mark content]
         content)
       (if (rum/react show-paths?) (str "|" path))]))

;we want to only call the above for operators, not vectors

(defc node-disp [node path] ;now *THIS* should take vectors, and everything else
  (into [:div #_(str (if (vector? node)
                       (str "vector mode:" node)
                       (str "non-vector-mode:" node))
                     ",   path:" path
                     ", path being passed: " (if (vector? node) (conj path 0) path))
         (if (vector? node)
           (node-comp (first node) (conj path 0))
           (node-comp node path))]
        (map-indexed
         (fn [idx child]
           (let [p (conj path (inc idx))]
             (node-disp child p)))
         (children node))))

(defc vertical-expr-tree < rum/reactive [tree-atm]
  [:div
   [:div (str @tree-atm)]
   [:div (node-disp (first (rum/react tree-atm)) [0])]])


#_[:=
   [:sum [:plus 2] [:plus 3] [:plus 4]]
   [:sum [:plus 999] [:minus [:paren [:sum [:plus 988] [:plus 2]]]]]]

                                        ;here beginneth the api





(defn reset-node [node v]
  v)

(defn update-node [node f & args]
  (apply f node args))

;-----the above two shouldn't exist.  when the below fns call `real-path`, they handle the ambiguity the `vector?` call is necessary for

(defn dotota [f & args] ;doto t.ree a.tom
  (apply swap! tree-atom f args ))

(defn reset-at-path! [p v]
  (if (= p [])
    (reset! tree-atom [v])
    (dotota assoc-in p v)))

(defn full-reset-at-path! [p v]
  (if (= p [])
    (reset! tree-atom v)
    (dotota assoc-in p v)))

(defn update-at-path! [p f & args]
  (if (= p [])
    (dotota  #(apply update-node % f args))
    (dotota update-in p #(apply update-node % f args))))

(defn append-at-path! [p v]
  (update-at-path! p (comp js/parseInt str) v))

(defn val-at [p]
  (vget-in @tree-atom p))

(defn down! []
  (cond (vector? (last @highlight-atom))
        nil ;we don't go down multiple paths

        (vector? @state/curr-value)
        (swap! highlight-atom conj 1)

        (= 0 (last @highlight-atom))
        (swap! highlight-atom vassoc -1 1)
        :else nil)
  #_(swap! highlight-atom tree-ops/down @tree-atom ))

(defn up! []
  (cond (> (count @highlight-atom) 1)
        (swap! highlight-atom tree-ops/prim-up @tree-atom)))

(defn left! []
  (swap! highlight-atom tree-ops/left @tree-atom))

(defn right! []
  (swap! highlight-atom tree-ops/right @tree-atom))

(defn select-operator! []
  (cond (vector? (vget-in @tree-atom @highlight-atom))
        (swap! highlight-atom conj 0)))

(defn children! []
  (swap! highlight-atom conj :children)) ;unimplemented, but should be---we *do* want to allow for selecting >1 node

(defn select-top! []
  (reset! highlight-atom [0]))

(defn extend-right! []
  (swap! highlight-atom tree-ops/extend-right @tree-atom))

(defn extend-left! []
  (swap! highlight-atom tree-ops/extend-left @tree-atom))

(defn delete! []
  (dotota delete-at @highlight-atom))

(defn toggle-parens! []
  (let [prev @curr-value
          lp (last @highlight-atom)
        
          type (cond (vector? lp)   ;:range within list/parens     ;wrap in parens
                     (do (swap! tree-atom update-in @parent-path #(-> %
                                                                     (vremove (first lp) (second lp))
                                                                     (vinsert (first lp) (into [:paren] prev))))
                         (swap! highlight-atom replace-last (first lp)))

                     (not (vector? prev))
                     (swap! tree-atom assoc-in @highlight-atom [:paren prev])
                     
                     (vector? prev)
                     (case (first prev)
                                        ;:paren             ;remove parens and splice
                       :paren
                       (do (swap! tree-atom update-in @parent-path #(-> %
                                                                        (vremove lp)
                                                                        (vsplice lp (rest prev))))
                           (if (> (count prev) 2)
                             (swap! highlight-atom replace-last [lp (+ lp (dec (count prev)))])))
                       :list
                       (do (swap! tree-atom assoc-in @highlight-atom [:list
                                                                      (into [:paren]
                                                                            (rest prev))])
                           (swap! highlight-atom conj 1))

                       (swap! tree-atom assoc-in @highlight-atom [:paren prev]))
                     


                     
                     ;:tagged-list ;literally should only matter at top level of expression
                     ;but currently shows up anywhere there's an infix i think
                     
                     #_(vector? prev)
                     ;:non-composite ;is now first (and only) member of :paren
                     )

        

        ]

      )

  #_(dotota update-in @highlight-atom
          (fn [prev-value]
            (if (and (vector? prev-value)
                     (= (first prev-value) :paren))
              (second prev-value)
              [:paren prev-value]))))

(defn raise! []
  (swap! tree-atom update-in (remove-last @highlight-atom)
         (fn [prev-value]
           (prev-value (last @highlight-atom)))))




{"list of paredit commands"
 [:open-round
  :close-round
  :wrap-round
  :the-deletes ;but keeps parens balanced
  [:c-d :one-char
   :m-d :one-word
   :backspace :one-char
   :m-del :kill-word
   :c-k :kill
   :c-close-round :slurp
   :splicing :m-up :m-down :m-s
   :split :m-S :join :m-j
   {:navigation :stuff}
   "also loads carried by cursor movement and highlighting"
   ]
  ]
 }
