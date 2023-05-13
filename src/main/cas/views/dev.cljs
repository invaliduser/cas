(ns cas.views.dev
  (:require
   [rum.core :as rum]
   cas.comps.main))

(rum/defc dev-page []
  (cas.comps.main/main-comp))
