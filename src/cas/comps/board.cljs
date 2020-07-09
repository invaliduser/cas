(ns cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths? roadmap atom-map]]
            [cas.keys :refer [key-stream-display]]
            [react]))

(def tokenized-style
    [:div {:style {:text-align "center" :font-size 24} #_#_:on-click #(reset! keystream '())}
     [:span {:style { }} #_(if (seq ktoke)
                           (apply str "tokenized: " (interpose " " ktoke)))]])


(add-watch tree-atom :to-tex (fn [k r o n]
                               (reset! tex (try (compile-to-tex (first n))
                                                (catch :default e (str e))
                                                )))) ;this might be a case for reframe...


(rum/defc full-tex-display < rum/reactive [tex-atom]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (or (rum/react tex-atom) "")))}}])

(rum/defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])





(rum/defc render-roadmap < rum/reactive [rm]
  [:div (for [{ename :name :as entry} rm]
          [:div {:key (:k entry)}
           [:div [:label ename]]
           [:div [:pre [:code (str (rum/react (atom-map ename)))]]]])])


(defn shove-pipe [s]
  (reset! (atom-map "tokenize-material") s))


(rum/defc backdrop < rum/reactive []
  [:div
   [:span {:on-click #(swap! show-paths? not)}  (str
                                                 "path"
                                                 (if (rum/react show-paths?) "(T)" "(F)") ":"
                                                 (rum/react highlight-atom))]
   (mode-indicator)
   [:hr]

   (render-roadmap roadmap)
   (full-tex-display (atom-map "compiled-to-tex"))


   (comment @tree-atom @(atom-map "parsed"))
   #_#_#_#_(easy-tree/atwrap (atom-map "parsed")) 

   (easy-tree/atwrap tree-atom)  
   (full-tex-display tex)
   (key-stream-display)
])



                                        ;big.
                                        ;toggleable, independent, "views" into the pipeline
                                        ;keystream---no
                                        ;what's being tokenized, how it's being tokenized---yes

                                        ;a manipulang view --- yes, but a very *friendly* one, hopefully 
                                        ;a latex view ---definitely



                                        ;another starting point is a state trunk
                                        ;don't work on this yet, but make the above components flexible so they can accept from any source

                                        ;and the other good starting point is input/keys/event management


                                        ;but before anything else, bridge the gap to latex
