(ns cas.keys
  (:require [goog.events :as events]
            [cljs.core.async :refer [chan <! >! go]]
            [cas.chans :refer [key-chan]]
            [cas.state :refer [mode]]
            )
  (:import [goog.events KeyHandler]
           [goog.events.EventType]))


(def digits
  #{\1 \2 \3 \4 \5 \6 \7 \8 \9 \0})


(def letters
  #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z })





(def hotkeys-map
  {"p" "+"
   ;"w" :start-write-mode
;   "e" :start-edit-mode
   "m" "-"
   "t" "*"
   "f" "/" ;?
   "o" :open
   "c" :close
   "r" :record
   ;"m" :minimize
   
   "ArrowRight" :right
   "ArrowLeft" :left
   "ArrowUp" :up
   "ArrowDown" :down
   "d" :down
   "i" :integral
   "=" :equal})

(def last-key (atom nil))


(defn key-down-listener [ev]
  (let [k (.-key ev)]
    (reset! last-key ev)

    (println "pressed " k " in " @mode " mode")
    
    (if (= k \\)
      (let [new-mode (case @mode :write :edit :edit :write)]
        (reset! mode new-mode)
        (println "setting to " new-mode " mode")))



    (case @mode
      :edit
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v)))
        (js/console.log (str k " pressed, no action associated...")))


      :write
      :default
      
      )))

(defn refresh-listeners []
  (events/unlisten (.-body js/document)
                   (.-KEYDOWN events/EventType)
                   key-down-listener)

  (events/listen (.-body js/document)
                 (.-KEYDOWN events/EventType)
                 key-down-listener))
