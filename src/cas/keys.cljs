(ns cas.keys
  (:require [goog.events :as events])

  (:import [goog.events KeyHandler]
           [goog.events.EventType]))



(def hotkeys-map
  {"p" +
   "m" -
   "t" *
   "f" / ;?
   "o" :open
   "c" :close
   "r" :root

   "d" :differentiate
   "i" :integral
   "=" :equal})

(events/listen (.-body js/document)
               (.-KEYDOWN events/EventType)
               #(let [k (.-key %)]
                  (js/console.log k)
                  (js/console.log (hotkeys-map k))))



