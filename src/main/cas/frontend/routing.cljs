(ns cas.frontend.routing
  (:require [reitit.frontend.easy :as rfe]
            [rum.core :as rum :refer-macros [defc]]
            [reitit.frontend :as rf]
            [cas.views.cockpit]
            [cas.views.dev]
            [cas.views.tree-manip :as tree-manip]))

(defc placeholder []
  "hi there")

(def current-view (atom placeholder))

(defc routing-component < rum/reactive []
  ((rum/react current-view)))

(def last-args (atom nil))

(defn on-navigate-fn [match history]
  (println "navigated!")
  (reset! last-args [match history])
  (reset! current-view (get-in match [:data :handler])))

(def basic-routes
  [
   ["/dev" {:handler cas.views.dev/dev-page :name :dev }]
   ["/tree-manip" {:handler tree-manip/tree-manip-harness :name :tree-manip}]
   ["/cockpit" {:handler cas.views.cockpit/cockpit-page :name :cockpit}]])

(defc basic-component []
  [:div (into [:ul]
              (map (fn [route]
                     [:li [:a {:on-click #(do
                                            (println "clicked")
                                            (rfe/navigate (get-in route [1 :name])))}
                           (first route)]])
                   basic-routes))])

(def opts {:use-fragment false})
(def routes
  (into [["/" basic-component]]
        basic-routes))

(def router (rf/router routes))

(defn start! []
  (rfe/start!
   router
   on-navigate-fn
   opts))
