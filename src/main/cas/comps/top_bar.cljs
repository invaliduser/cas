(ns cas.comps.top-bar
  (:require
   [rum.core :as rum]
   ["@mui/material" :as mui]))

(rum/defc TopBar []
  [:> mui/AppBar {:position "static"}
   [:> mui/Box
    [:span "hi there"]]])
