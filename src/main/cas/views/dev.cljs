(ns cas.views.dev
  (:require
   [rum.core :as rum :refer-macros [defc]]
   cas.comps.main))

(defc dev-page []
  (cas.comps.main/main-comp))
