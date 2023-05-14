(ns cas.routing
  (:require [reitit.frontend.easy :as rfe]
            [rum.core :as rum]
            [reitit.frontend :as rf]
            [cas.views.cockpit]
            [cas.views.dev]
            [cas.views.tree-manip :as tree-manip]))

(rum/defc placeholder []
  "hi there")

(def current-view (atom placeholder))

(rum/defc routing-component < rum/reactive []
  (@current-view))

(def last-args (atom nil))

(defn on-navigate-fn [match history]
  (reset! last-args [match history])
  (reset! current-view (get-in match [:data :handler])))

(rum/defc basic-component []
  "basic component, reachable only by routing")

(def opts {:use-fragment false})
(def routes
  [["/" basic-component]
   ["/dev" cas.views.dev/dev-page]
   ["/tree-manip" tree-manip/tree-manip-harness]
   ["/cockpit" cas.views.cockpit/cockpit-page]
   

])

(def router (rf/router routes))

(defn start! []
  (rfe/start!
   router
   on-navigate-fn
   opts))
