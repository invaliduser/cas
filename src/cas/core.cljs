(ns cas.core
  (:require [cas.utils :refer [ alert]]
            [rum.core :as rum]
            [cas.comps]
            [cas.keys :refer [refresh-listeners]]))


(set! *warn-on-infer* true)

(enable-console-print!)


(defonce app-state (atom {:text "Hello world!"}))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)


(defn init! []
  (rum/mount (cas.comps/main-comp) (js/document.querySelector "#app"))
  (refresh-listeners))


