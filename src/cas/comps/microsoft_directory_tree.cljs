(ns cas.comps.microsoft-directory-tree
  (:require [rum.core :as rum]
            [cas.tex-render :refer [render-tex]]
            [cljs.core.async :refer [chan <! >! go-loop]]
            [cas.state :refer [tree-atom highlight-atom show-paths? all-real-path]]
            [cas.tree-ops :refer [real-path children children? represents-fn? remove-last doto-last node-val tree-get]]
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


(rum/defcs node-comp < rum/reactive [state node path]
  (let [content (-> node node-val str)
        condition-f (if (rum/react all-real-path)
                      matches-real-path?
                      matches-path?)]
    #_[:span {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex node))}}]

    [:span {:style {:margin-left (* 10 (count path))}
            :on-click #(reset! highlight-atom path)} 
     (if (condition-f path (rum/react highlight-atom))

         #_(= path (rum/react highlight-atom))


       [:mark content]

       content)
     (if (rum/react show-paths?) (str "|" path))
     #_(str (tree-get @tree-atom path) #_(= node-val (get-in @tree-atom path)))]))

(rum/defc node-disp [node path]
  (into [:div] (concat [(node-comp node path)]
                       (map-indexed (fn [idx node]
                                      (let [p (conj path idx)]
                                        (node-disp node p)))
                                    (children node)))))


#_(rum/defc real-path-node-disp [node path]
  (if-not (children? node)
    [:div (node-comp node path)]
    [:div (concat [(node-comp node path)]
                  (map-indexed (fn [idx node]
                                 (let [p (conj path (inc idx))]
                                   (real-path-node-disp node p)))
                               (children node)))]))

#_[:=
   [:sum [:plus 2] [:plus 3] [:plus 4]]
   [:sum [:plus 999] [:minus [:paren [:sum [:plus 988] [:plus 2]]]]]]


(rum/defc atwrap < rum/reactive [tree-atm]
  (cond
    #_#_(true? @all-real-path)
    (real-path-node-disp  (first (rum/react tree-atm)) [0])
    (false? @all-real-path)
    (node-disp (first (rum/react tree-atm)) [0])))

                                        ;here beginneth the api





(defn down! []
  (swap! highlight-atom conj 0))

(defn up! []
  (swap! highlight-atom remove-last))

(defn left! []
  (swap! highlight-atom doto-last dec))

(defn right! []
  (swap! highlight-atom doto-last inc))

(defn children! []
  (swap! highlight-atom conj :children))

(defn surround-with-parens! [p]
  (update-at-path! p)
  )

(action-interpreter "tree-manip" {:left left!
                                  :right right!
                                  :down down!
                                  :up up!
                                  :children children!
                                  :real-up #(swap! highlight-atom cas.tree-ops/real-up)
                                  :real-down #(swap! highlight-atom cas.tree-ops/real-down)
                                  :real-left #(swap! highlight-atom cas.tree-ops/real-left)                     
                                  :real-right #(swap! highlight-atom cas.tree-ops/real-right)
                                  #_:surround-with-parens

                                  }
                    key-chan
                    :after #(println (str "got " % ", path is now " @highlight-atom)))
