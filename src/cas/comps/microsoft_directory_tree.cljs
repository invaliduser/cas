(ns cas.microsoft-directory-tree
  (:require [rum.core :as rum]
            [clojure.walk :as walk]
            [cas.tex-render :refer [render-tex]]
            [cljs.core.async :refer [chan <! >! go-loop]]
            [cas.state :refer [tree-atom]]
            [cas.chans :refer [key-chan action-interpreter]]))

(def highlight-atom (atom [0]))

(rum/defcs node-comp < rum/reactive [state node path]
  (let [content (str node)
]
  #_[:span {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex node))}}]

    [:span {:style {:margin-left (* 10 (count path))}
            :on-click #(reset! highlight-atom path)}
     (if (= path (rum/react highlight-atom)) [:mark content] content )]))

(rum/defc node-disp [node path]
  (if (vector? node)
    [:div (concat [(node-comp (first node) path)]
                  (map-indexed #(node-disp %2 (conj path %)) (rest node)))]
    [:div (node-comp node path)]))

(rum/defc atwrap < rum/reactive [tree-atm]
  (node-disp (rum/react tree-atm) [0]))

;here beginneth the api

(defn down! []
  (swap! highlight-atom conj 0))

(let [remove-last (fn [v]
                    (subvec v 0 (dec (count v))))]
 (defn up! []
     (swap! highlight-atom remove-last)))

(let [update-last (fn [v f]
                    (update v (dec (count v)) f))]
  (defn left! []
    (swap! highlight-atom update-last dec))
  (defn right! []
    (swap! highlight-atom update-last inc)))



(defn reset-at-path [v p] ;val path
  (swap! tree-atom assoc-in p v)) 



(action-interpreter "tree-manip" {:left left!
                                  :right right!
                                  :down down!
                                  :up up!}
                    key-chan
                    :after #(println (str "got " % ", path is now " @highlight-atom)))
