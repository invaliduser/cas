(ns cas.frontend.core
  (:require [rum.core :as rum]
            [cas.frontend.comps.main]
            [cas.frontend.keys :refer [refresh-listeners]]
            [cas.frontend.routing :as routing]
            [cas.frontend.actions :as actions]
            [cas.frontend.comps.pdf :as pdf]))


(set! *warn-on-infer* true)

(enable-console-print!)


(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)


(defn init! []
  (routing/start!)
  (actions/init-actions!)
  (pdf/start-render-loop!)
  (rum/mount
   #_(cas.comps.main/main-comp)
   (routing/routing-component)
   (js/document.querySelector "#app"))
  (refresh-listeners))


