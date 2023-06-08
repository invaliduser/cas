(ns cas.comps.problem-drawer
  (:require
   [rum.core :as rum]
   ["@mui/material" :as mui]
   [cas.state]
   [cas.lang-to.mathml :refer [render-to-inert-mathml]]))


(rum/defc Problem < rum/reactive {:key-fn (fn [idx problem] idx)} [idx problem]
  (let [selected? (= idx (rum/react cas.state/selected-problem))]
    [:> mui/ListItem {:onClick #(reset! cas.state/selected-problem idx)
                      :style {:backgroundColor (if selected? "blue" "white")}}
     [:> mui/ListItemText (str (inc idx) ". ")]
     (render-to-inert-mathml (first problem))]))

(rum/defc Problem-Drawer < rum/reactive []
  [:> mui/Drawer {:anchor "right"
                  :variant "permanent"}
   [:> mui/Box {:width 300}
    [:> mui/List 
     (map-indexed Problem #_Problem (rum/react cas.state/problems))]]])

(defn problem-up! []
  (swap! cas.state/selected-problem
         #(if (<= % 0)
            %
            (dec %))))

(defn problem-down! []
  (swap! cas.state/selected-problem
         #(if (>= % (dec (count @cas.state/problems )))
            %
            (inc %))))
