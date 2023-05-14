(ns cas.comps.problem-drawer
  (:require
   [rum.core :as rum]
   ["@mui/material" :as mui]
   [cas.lang-to.mathml :refer [render-to-inert-mathml]]))


(rum/defc Problem < {:key-fn (fn [idx problem] idx)} [idx problem]
  [:> mui/ListItem 
   [:> mui/ListItemText (str (inc idx) ". ")]
   (render-to-inert-mathml (first problem))])

(rum/defc Problem-Drawer []
  [:> mui/Drawer {:anchor "right"
                  :open true}
   [:> mui/Box {:width 300}
    [:> mui/List 
     (map-indexed Problem #_Problem @cas.state/problems)]]])

  
