(ns cas.microsoft-directory-tree
  (:require [rum.core :as rum]
            [cas.tex-render :refer [render-tex]]
            [cljs.core.async :refer [chan <! >! go-loop]]
            [cas.state :refer [tree-atom highlight-atom show-paths?]]
            [cas.tree-ops :refer [children children? represents-fn? remove-last doto-last node-val tree-get]]
            [cas.chans :refer [key-chan action-interpreter]]))


(rum/defcs node-comp < rum/reactive [state node path]
  (let [content (-> node node-val str)]
    #_[:span {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex node))}}]

    [:span {:style {:margin-left (* 10 (count path))}
            :on-click #(reset! highlight-atom path)} 
     (if (= path (rum/react highlight-atom)) [:mark content] content)
     (if (rum/react show-paths?) (str "|" path))
     #_(str (tree-get @tree-atom path) #_(= node-val (get-in @tree-atom path)))]))

(rum/defc node-disp [node path]
  (if-not (children? node)
    [:div (node-comp node path)]
    [:div (concat [(node-comp node path)]
                  (map-indexed (fn [idx node]
                                 (let [p (conj path idx)]
                                   (node-disp node p)))
                               (children node)))]))


(rum/defc atwrap < rum/reactive [tree-atm]
  (node-disp (rum/react tree-atm) [0]))

                                        ;here beginneth the api

(defn down! []
  (swap! highlight-atom conj 0))

(defn up! []
  (swap! highlight-atom remove-last))

(defn left! []
  (swap! highlight-atom doto-last dec))

(defn right! []
  (swap! highlight-atom doto-last inc))

(action-interpreter "tree-manip" {:left left!
                                  :right right!
                                  :down down!
                                  :up up!}
                    key-chan
                    :after #(println (str "got " % ", path is now " @highlight-atom)))
