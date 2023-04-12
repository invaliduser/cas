(ns cas.comps.basics
  (:require [rum.core :as rum]
            [cas.tex-render :refer [render-tex]]))


(rum/defc editable-textarea < rum/reactive [atm]
  [:div
   [:textarea {:value (rum/react atm)
               :on-change (fn [e] (reset! atm (.. e -target -value)))}]])

(rum/defc full-tex-display < rum/reactive [tex-atom]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (or (rum/react tex-atom) "")))}}]
)


