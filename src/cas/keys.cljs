(ns cas.keys
  (:require [goog.events :as events]
            [cljs.core.async :refer [chan <! >! go]]
            [cas.chans :refer [key-chan]]
            [cas.state :refer [mode was-write-mode-before? highlight-atom]]
            [cas.tree-ops :refer [reset-at-path! append-at-path!]]
            [cas.manipulang :refer [digits letters]])
  (:import [goog.events KeyHandler]
           [goog.events.EventType]))




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
        (if (= new-mode :write)
          (reset! was-write-mode-before? false))
        (reset! mode new-mode)
        (println "setting to " new-mode " mode")))



    (case @mode
      :edit
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v)))
        (js/console.log (str k " pressed, no action associated...")))


      :write ;getting into write mode is a one-time thing,
      (cond (digits k)
            (if @was-write-mode-before?
              (append-at-path! @highlight-atom k)
              (do (reset-at-path! @highlight-atom k)
                  (reset! was-write-mode-before? true))))

      :default nil
      
      )))

(defn refresh-listeners []
  (events/unlisten (.-body js/document)
                   (.-KEYDOWN events/EventType)
                   key-down-listener)

  (events/listen (.-body js/document)
                 (.-KEYDOWN events/EventType)
                 key-down-listener))
