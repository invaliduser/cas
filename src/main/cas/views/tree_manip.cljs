(ns cas.views.tree-manip
  (:require [rum.core :as rum :refer-macros [defc]]
            [cas.comps.microsoft-directory-tree :as ms-dir-tree]
            [cas.frontend.state :refer [mode tree-atom tex highlight-atom show-paths?]]
            [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.lang-to.mathml :refer [render-to-navigable-mathml]]
            [cas.comps.basics :as basics]))

(defc top-status < rum/reactive []
  [:div [:span {:on-click #(swap! show-paths? not)}
           (str "path" "(T)" ":" (rum/react highlight-atom))]])

(defc tree-manip-harness < rum/reactive []
  [:div #_(top-status)
   [:hr]
   (ms-dir-tree/vertical-expr-tree tree-atom)
   [:hr]
   (render-to-navigable-mathml (first @tree-atom)) 
   #_(basics/full-tex-display tex)])
