(ns cas.frontend.views.cockpit
  (:require [rum.core :as rum :refer-macros [defc]]
            [cas.frontend.comps.equation]
            [cas.frontend.state :refer [tree-atom]]
            [cas.frontend.comps.problem-drawer :as pd]
            [cas.frontend.comps.top-bar :as top-bar]
            [cas.frontend.comps.pdf :as pdf]))

;note: snappiness, quick responsiveness, and few-steps-to-destination covereth a multitude of design sins


;context experiment
(rum/defcontext *context*)

#_(defc consumer []
  (rum/with-context [n *context*]
    [:span n]))
#_(defc provider []
  (rum/bind-context [*context* 65]
                    (consumer)))

(defc cockpit-page < rum/reactive []
  [:div.container
   (top-bar/TopBar)
   [:div
    (cas.frontend.comps.equation/mathml-eqn tree-atom)]
   [:div (pdf/pdf-canvas)]
   [:div "pd" (pd/Problem-Drawer)]])
