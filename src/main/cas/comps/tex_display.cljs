(ns cas.comps.tex-display
  (:require [rum.core])
  )



(rum/defc full-tex-display < rum/reactive [tex-atom]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (or (rum/react tex-atom) "")))}}])
