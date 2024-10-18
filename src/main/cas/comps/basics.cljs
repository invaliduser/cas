(ns cas.comps.basics
  (:require [rum.core :as rum :refer-macros [defc]]
            [cas.frontend.tex-render :refer [render-tex]]))


(defc editable-textarea < rum/reactive [atm]
  [:div
   [:textarea {:value (rum/react atm)
               :on-change (fn [e] (reset! atm (.. e -target -value)))}]])

(defc full-tex-display < rum/reactive [tex-atom]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (or (rum/react tex-atom) "")))}}]
)


