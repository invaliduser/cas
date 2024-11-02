(ns ^:figwheel-always cas.frontend.comps.board
  (:require [rum.core :as rum :refer-macros [defc]]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.frontend.comps.path-tree :as easy-tree]
            [cas.frontend.tex-render :refer [render-tex]]
            [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.frontend.state :refer [mode tree-atom tex atom-map] :as state]
            [cas.frontend.keys :refer [key-stream-display]]
            [cas.frontend.comps.basics :as basics]
            [react]))

(defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])

(defc render-roadmap < rum/reactive []
  [:div (for [k (keys atom-map)]
          [:div {:key (str k)}
           [:div [:label (str k)]]
           [:div (str (rum/react (atom-map k)))]])])


(defn shove-pipe [s]
  (reset! (atom-map :untokenized) s))

(def tholder (atom ""))

(defc backdrop < rum/reactive []
  [:div
   
   (mode-indicator)
   [:hr]

   [:div [:textarea {:on-change (fn [e] (reset! tholder (.. e -target -value )))}]

    [:div [:code (str (rum/react tholder))]]]
   
   (render-roadmap)
   (basics/full-tex-display (atom-map :raw-tex))


   (key-stream-display)
   [:div [:input {:type "button"
                  :on-click #(do
                               (reset! tree-atom [@(state/atom-map :parsed)])
                               (state/advance!)
                               (state/advance!))
                  :value "move to tree"}]]])

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
