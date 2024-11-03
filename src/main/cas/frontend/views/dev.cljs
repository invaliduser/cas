(ns cas.frontend.views.dev
  (:require
   [rum.core :as rum :refer-macros [defc]]
   cas.frontend.comps.main))

(defc dev-page []
  (cas.frontend.comps.main/main-comp))
