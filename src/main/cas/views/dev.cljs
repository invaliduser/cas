(ns cas.views.dev
  (:require
   [rum.core :as rum :refer-macros [defc]]
   cas.frontend.comps.main))

(defc dev-page []
  (cas.comps.main/main-comp))
