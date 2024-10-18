(ns cas.comps.problem-drawer
  (:require
   [rum.core :as rum :refer-macros [defc]]
   ["@mui/material" :as mui]
   [cas.frontend.state :as state]
   [cas.lang-to.mathml :refer [render-to-inert-mathml]]))


(defc Problem-List-Item < rum/reactive {:key-fn (fn [idx problem] idx)} [idx problem]
  (let [selected? (= idx (rum/react state/selected-problem))]
    [:> mui/ListItem {:onClick #(reset! state/selected-problem idx)
                      :style {:backgroundColor (if selected? "blue" "white")}}
     [:> mui/ListItemText (str (inc idx) ". ")]
     (render-to-inert-mathml (first problem))]))

(defc Problem-Drawer < rum/reactive []
  [:> mui/Drawer {:anchor "right"
                  :variant "permanent"}
   [:> mui/Box {:width 300}
    [:> mui/List 
     (map-indexed Problem-List-Item (rum/react state/problems))]]])

(defn problem-up! []
  (swap! state/selected-problem
         #(if (<= % 0)
            %
            (dec %))))

(defn problem-down! []
  (swap! state/selected-problem
         #(if (>= % (dec (count @state/problems )))
            %
            (inc %))))
