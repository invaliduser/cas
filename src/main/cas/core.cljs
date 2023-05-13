(ns cas.core
  (:require [cas.utils :refer [alert]]
            [rum.core :as rum]
            [cas.comps.main]
            [cas.keys :refer [refresh-listeners]]
            [cas.routing]))


(set! *warn-on-infer* true)

(enable-console-print!)


(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)


(defn init! []
  (cas.routing/start!)
  (rum/mount
   #_(cas.comps.main/main-comp)
   (cas.routing/routing-component)
   (js/document.querySelector "#app"))
  (refresh-listeners))


