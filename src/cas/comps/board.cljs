(ns cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.rein-til :as r-t]
            [cas.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex]]
            [react]))

(add-watch tree-atom :to-tex (fn [k r o n]
                           (reset! tex (compile-to-tex n)))) ;this might be a case for reframe...


(rum/defc full-tex-display < rum/reactive []
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (rum/react tex)))}}])


(rum/defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])

(rum/defc backdrop < rum/reactive []
  [:div
   (mode-indicator)
   [:hr]
   (easy-tree/atwrap tree-atom)
   (full-tex-display (compile-to-tex (rum/react tree-atom)))])


