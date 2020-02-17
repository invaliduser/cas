(ns cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.rein-til :as r-t]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths?]]
            [cas.comps.keylang :refer [key-stream-display]]
            [react]))

(add-watch tree-atom :to-tex (fn [k r o n]
                           (reset! tex (compile-to-tex n)))) ;this might be a case for reframe...


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
   (key-stream-display)
   (full-tex-display (try (compile-to-tex (rum/react tree-atom))
                          (catch :default e (str e))
                          ))])

