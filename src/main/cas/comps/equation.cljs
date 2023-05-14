(ns cas.comps.equation
  (:require [rum.core :as rum]
            [cas.lang-to.mathml :refer [render-to-navigable-mathml]]))

(rum/defc mathml-eqn < rum/reactive [atm]
  [:div
   (render-to-navigable-mathml (first (rum/react atm)))])



