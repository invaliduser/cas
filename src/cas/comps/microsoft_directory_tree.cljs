(ns cas.comps.microsoft-directory-tree
  (:require [rum.core :as rum]
            [cas.tex-render :refer [render-tex]]
            [cljs.core.async :refer [chan <! >! go-loop]]
            [cas.state :refer [tree-atom highlight-atom show-paths? all-real-path]]
            [cas.tree-ops :refer [real-path children children? represents-fn? remove-last doto-last node-val tree-get nodal-descendant logical-descendant update-at-path! vassoc]]

            [cas.chans :refer [key-chan action-interpreter]]
            [cas.shorthand :as sh]
            ))


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


(rum/defc node-comp < rum/reactive [node path]
  (let [content (-> node node-val str)
        condition-f (if (rum/react all-real-path)
                      matches-real-path?
                      matches-path?)]
    #_[:span {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex node))}}]

    [:span {:style {:margin-left (* 10 (count path))}
            :on-click #(reset! highlight-atom path)} 
     (if (condition-f path (rum/react highlight-atom))
       [:mark content]
       content)
     (if (rum/react show-paths?) (str "|" path))
     #_(str (tree-get @tree-atom path) #_(= node-val (get-in @tree-atom path)))]))

(rum/defc real-path-node-comp < rum/reactive [node path]
  ;current node-comp doesn't worry about children or even the node, neither will we
  ; we will not call this on node
  #_[:div
   [:span "node: " (str node) ", "]
   [:span "path: " (str path)]
   [:span "path result: " (str (get-in @tree-atom path ))]
   [:span ", worked: " (str (= (get-in @tree-atom path) node))]
   [:span ", logical-descendant? " (str (logical-descendant @highlight-atom path))]
   
   ]
  
  (let [content (str node)]
    [:span {:style {:margin-left (-> (count path)
                                     (+ (if (= (last path) 0) 0 1))
                                     (* 10))

                    #_(* 10
                       (+ (count path) (if (= (last path) 0) 1 0)))} ;will prolly need fixing
              :on-click #(reset! highlight-atom path)}
       (if (or
            ;need one for exact match, e.g. way to highlight a =
            (nodal-descendant (rum/react highlight-atom) path)
            #_(logical-descendant (rum/react highlight-atom) path))
         [:mark content]
         content)
       (if (rum/react show-paths?) (str "|" path))])) 

;we want to only call the above for operators, not vectors

(rum/defc real-path-node-disp [node path] ;now *THIS* should take vectors, and everything else
  
  (into [:div #_(str (if (vector? node)
                     (str "vector mode:" node)
                     (str "non-vector-mode:" node))
                   ",   path:" path
                   ", path being passed: " (if (vector? node) (conj path 0) path))

         (if (vector? node)
             (real-path-node-comp (first node) (conj path 0))
             (real-path-node-comp node path))
         ]
        (map-indexed
         (fn [idx child]
           (let [p (conj path (inc idx))]
             (real-path-node-disp child p)))
         (children node))))

(rum/defc node-disp [node path]
  (into [:div] (concat [(node-comp node path)]
                       (map-indexed (fn [idx node]
                                      (let [p (conj path idx)]
                                        (node-disp node p)))
                                    (children node)))))


#_[:=
   [:sum [:plus 2] [:plus 3] [:plus 4]]
   [:sum [:plus 999] [:minus [:paren [:sum [:plus 988] [:plus 2]]]]]]


(rum/defc atwrap < rum/reactive [tree-atm]
  (cond
    (true? @all-real-path)
    [:div
     [:div (str @tree-atm)]
     [:div (real-path-node-disp (first (rum/react tree-atm)) [0])]]
    (false? @all-real-path)
    (node-disp (first (rum/react tree-atm)) [0])))

                                        ;here beginneth the api



(defn val-at [p]
  (get-in @tree-atom p))

(defn current-val []
  (get-in @tree-atom @highlight-atom))

                      
                                   
(defn down! []
  (cond (vector? (current-val))
        (swap! highlight-atom conj 1)
        (= 0 (last @highlight-atom))
        (swap! highlight-atom vassoc -1 1)
        :else :nothing)

  #_(swap! highlight-atom cas.tree-ops/down @tree-atom ))

(defn up! []
  (cond (> (count @highlight-atom) 1)
        (swap! highlight-atom cas.tree-ops/prim-up @tree-atom)))

(defn left! []
  (swap! highlight-atom cas.tree-ops/left @tree-atom))

(defn right! []
  (swap! highlight-atom cas.tree-ops/right @tree-atom))

(defn select-operator! []
  (cond (vector? (get-in @tree-atom @highlight-atom))
        (swap! highlight-atom conj 0)))

(defn children! []
  (swap! highlight-atom conj :children)) ;unimplemented, but should be---we *do* want to allow for selecting >1 node

(defn select-top! []
  (reset! highlight-atom [0]))

(defn surround-with-parens! []
  (swap! tree-atom update-in @highlight-atom
         (fn [prev-value] [:paren prev-value])))

(defn raise! []
  (swap! tree-atom update-in (remove-last @highlight-atom)
         (fn [prev-value]
           (prev-value (last @highlight-atom)))))


(action-interpreter "tree-manip" {:left left!
                                  :right right!
                                  :down down!
                                  :up up!
                                  :select-operator select-operator!
                                  :surround-with-parens surround-with-parens!
                                  :select-top select-top!
                                  }
                    key-chan
                    :after #(println (str "got " % ", path is now " @highlight-atom)))
