(ns cas.comps.top-bar
  (:require
   [rum.core :as rum :refer-macros [defc]]
   ["@mui/material" :as mui]))

(defc TopBar []
  [:> mui/AppBar {:position "static"}
   [:> mui/Box
    [:span "hi there"]]])
