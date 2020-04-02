(ns cas.core
  (:require [cas.utils :refer [parse compile evaluate simplify alert]]
            [rum.core :as rum]
            [cas.comps]
            [cas.keys :refer [refresh-listeners]]))


(set! *warn-on-infer* true)

(enable-console-print!)



(println "This text is printed from src/cas/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)




(rum/mount (cas.comps/main-comp) (js/document.querySelector "#app"))

#_(alert (simplify  "3+2+11"))
(refresh-listeners)
