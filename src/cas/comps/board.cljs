(ns ^:figwheel-always cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex roadmap atom-map]]
            [cas.keys :refer [key-stream-display]]
            [cas.comps.basics :as basics]
            [react]))

(rum/defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])

(rum/defc render-roadmap < rum/reactive [rm]
  [:div (for [{ename :name :as entry} rm]
          [:div {:key (:k entry)}
           [:div [:label ename]]
           [:div (str (rum/react (atom-map ename)))]])])


(defn shove-pipe [s]
  (reset! (atom-map "tokenize-material") s))

(def tholder (atom ""))

(rum/defc backdrop < rum/reactive []
  [:div
   
   (mode-indicator)
   [:hr]

   [:div [:textarea {:on-change (fn [e] (reset! tholder (.. e -target -value )))}]

    [:div [:code (str (rum/react tholder))]]]
   
   (render-roadmap roadmap)
   (basics/full-tex-display (atom-map "compiled-to-tex"))


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
