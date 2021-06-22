(ns cas.views.tree-manip
  (:require [rum.core :as rum]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths? roadmap atom-map]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.comps.basics :as basics]))


(add-watch tree-atom :to-tex (fn [k r o n]
                               (reset! tex (try (compile-to-tex (first n))
                                                (catch :default e (str e))
                                                ))))

(rum/defc top-status < rum/reactive []
  [:div [:span {:on-click #(swap! show-paths? not)}
         (str "path" (if (rum/react show-paths?)
                       "(T)" "(F)") ":" (rum/react highlight-atom))]])

(rum/defc tree-manip-harness < rum/reactive []
  [:div (top-status)
   [:hr]
   (easy-tree/atwrap tree-atom)
   [:hr]
   (basics/full-tex-display tex)])
