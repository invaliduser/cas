(ns cas.views.tree-manip
  (:require [rum.core :as rum]
            [cas.comps.microsoft-directory-tree :as ms-dir-tree]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths? roadmap atom-map]]
            [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.lang-to.mathml :refer [render-to-navigable-mathml]]
            [cas.comps.basics :as basics]))

(rum/defc top-status < rum/reactive []
  [:div [:span {:on-click #(swap! show-paths? not)}
           (str "path" (if (rum/react cas.state/all-real-path)
                         "(T)" "(F)") ":" (rum/react highlight-atom))]])

(rum/defc tree-manip-harness < rum/reactive []
  [:div #_(top-status)
   [:hr]
   (ms-dir-tree/vertical-expr-tree tree-atom)
   [:hr]
   (render-to-navigable-mathml (first @tree-atom)) 
   #_(basics/full-tex-display tex)])
