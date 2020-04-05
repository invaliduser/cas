(ns cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.rein-til :as r-t]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths?]]
            [cas.keys :refer [key-stream-display]]
            [cas.nat :refer [combos-display]]
            [react]))

(add-watch tree-atom :to-tex (fn [k r o n]
                               (reset! tex (try (compile-to-tex n)
                                                (catch :default e (str e))
                                                )))) ;this might be a case for reframe...


(rum/defc full-tex-display < rum/reactive []
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (rum/react tex)))}}])


(rum/defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])

(rum/defc backdrop < rum/reactive []
  [:div
   [:span {:on-click #(swap! show-paths? not)}  (str
                                                 "path"
                                                 (if (rum/react show-paths?) "(T)" "(F)") ":"
                                                 (rum/react highlight-atom))]
   (mode-indicator)
   [:hr]
   (easy-tree/atwrap tree-atom)
   (full-tex-display (rum/react tex))
   (combos-display)
   (key-stream-display)
])


