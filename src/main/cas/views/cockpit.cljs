(ns cas.views.cockpit
  (:require [rum.core :as rum]
            [cas.comps.equation]
            [cas.state :refer [tree-atom]]
            [cas.comps.problem-drawer :as pd]
            [cas.comps.top-bar :as top-bar]))

;note: snappiness, quick responsiveness, and few-steps-to-destination covereth a multitude of design sins


;context experiment
(rum/defcontext *context*)

#_(rum/defc consumer []
  (rum/with-context [n *context*]
    [:span n]))
#_(rum/defc provider []
  (rum/bind-context [*context* 65]
                    (consumer)))

(rum/defc cockpit-page < rum/reactive []
  [:div.container
   #_(top-bar/TopBar)
   [:div
    [:span (str (rum/react tree-atom))]
    (cas.comps.equation/mathml-eqn tree-atom )]
   [:div "right drawer---set of problems"
    (pd/Problem-Drawer)]])
