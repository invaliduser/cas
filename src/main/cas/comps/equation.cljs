(ns cas.comps.equation
  (:require [rum.core :as rum :refer-macros [defc]]
            [cas.lang-to.mathml :refer [render-to-navigable-mathml]]))

(defc mathml-eqn < rum/reactive [atm]
  [:div
   (render-to-navigable-mathml (first (rum/react atm)))])



