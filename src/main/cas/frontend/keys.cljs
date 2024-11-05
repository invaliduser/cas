(ns cas.frontend.keys
  (:require [goog.events :as events]
            [rum.core :as rum :refer-macros [defc]]
            [cljs.core.async :refer [chan <! >! go]]
            [cas.frontend.chans :refer [key-chan]]
            [cas.frontend.state :refer [mode highlight-atom tree-atom keystream keystream-undecided last-key keylang-input write-buffer] :as state]
            [cas.frontend.nat :refer [full]]
            [cas.common.utils :refer [letters u-letters digit-strings operator-strings text-edit!-keys]]
            [cas.common.shorthand :as sh])
  (:import [goog.events KeyHandler]
           [goog.events.EventType]))



(def arrow-keys #{"ArrowRight" "ArrowLeft" "ArrowUp""ArrowDown"})

(defn alt+ [k ev]
  (and (.-altKey ev)
       (= k (.-key ev))))
(defn ctrl+ [k ev]
  (and (.-ctrlKey ev)
       (= k (.-key ev))))
(defn shift+ [k ev]
  (and (.-shiftKey ev)
       (= k (.-key ev))))

(defn serialize-key-event [ev]
  (let [adds (set (filter identity [(if (.-ctrlKey ev) :ctrl)
                                    (if (.-altKey ev) :alt)
                                    (if (.-shiftKey ev) :shift)]))]
    (if (seq adds)
      (conj adds (.-key ev)) ;TODO 
      (.-key ev))))


(def hotkeys-map
  {
                                        ;"w" :start-write-mode
                                        ;"e" :start-edit-mode
   "t" :select-top
   "o" :select-operator
   #_#_"c" :close
   "h" :children
   "s" :snapshot
                                        ;"m" :minimize

   "Delete" :delete
   "Backspace" :delete
   "p" :toggle-parens
   "r" :replace-with-authoring
   "d" :treat-as-argument-to            ;"doto"
   "ArrowRight" :right
   "ArrowLeft" :left
   "ArrowUp" :up
   "ArrowDown" :down
   "i" :integral
   "=" :equal
   
   #{:shift "ArrowRight"} :extend-right
   #{:shift "ArrowLeft"} :extend-left
   #{:shift "ArrowUp"} :up
   #{:ctrl "c"} :copy
   #{:ctrl "v"} :paste
   #{:ctrl "x"} :cut
   
   #{:ctrl "z"} :undo
   #{:alt "ArrowUp"} :problem-up
   #{:alt "ArrowDown"} :problem-down})

(defn send-to-key-chan-listener [handler]
  (fn [ev]
    (let [k (serialize-key-event ev)]
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v))
            (handler ev))
        (js/console.log (str k " pressed, no action associated..."))))))

(defn tree-manip-key-handler [handler]
  (fn [ev]
    (let [k (serialize-key-event ev)]
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v)))
        (js/console.log (str k " pressed, no action associated..."))))))


(defc key-stream-display < rum/reactive []
  [:div {:style {:text-align "center" :font-size 24} :on-click #(do
                                                                  (println "reset keystream")
                                                                  (reset! keystream []))}
   [:span "resolved here"]
   [:br]
   [:span {:style { }} (apply str (interpose " " (rum/react keystream)))]
   [:div {:style { }} (apply str "stack:" (interpose " " (rum/react keystream-undecided)))]])


(def tokenizeable-keys "All keys that can be tokenized."
  (set (concat u-letters letters digit-strings operator-strings)))


;key handlers are like middleware, taking another handler

(defn debug-key-listener [handler]
  (fn [ev]
    #_(reset! last-key ev)
    (println "pressed " (serialize-key-event ev) " in " @mode " mode")
    (js/console.log ev)
    (handler ev)))

(defn mode-switch-listener [handler]
  (fn [ev]
    (let [k (.-key ev)]
      (cond
        (or (and (= @mode :write)
                 (arrow-keys k))
            (#{\\ "Enter"} (.-key ev))) ;this should maybe be refactored out to somewhere else
        (let [new-mode (case @mode :write :edit :edit :write)]
          (if (= new-mode :write)
            (reset! state/write-buffer  (rum.core/cursor-in tree-atom @highlight-atom)))
          (reset! mode new-mode)
          (reset! keylang-input "")
          (println "setting to " new-mode " mode"))))
    (handler ev)))

(defn keystream-watch-listener [handler]
  (fn [ev]
    (let [k (.-key ev)]
      (if (not (arrow-keys k))
        (swap! keystream conj k))
      (handler ev))))

(defn default-preventer [handler]
  (fn [ev]
    (if-not (or (ctrl+ "r" ev)
                (#{"F12" "Tab" " " "Shift"} (.-key ev)))
      (.preventDefault ev))
    (handler ev)))

(defn tokenizeable-key-handler [handler atm] ; need to write a cursor for this
  (fn [ev]
    (let [k (.-key ev)]
      (if (or (text-edit!-keys k)
              (tokenizeable-keys k))
        (do
          (println k)
          (case k
            "Backspace"
            (swap! atm #(.slice % 0 -1))
            (swap! atm str k))))
      (handler ev))))

(defn write-mode-listener [handler]
  (fn [ev]
    (let [k   (.-key ev)]
      (if (or (text-edit!-keys k)
              (tokenizeable-keys k))
        (do        (case k
                     "Backspace"
                     (swap! state/keylang-input #(.slice % 0 -1))
                     (swap! state/keylang-input str k))
                   (println @keylang-input)
                   (println (full @keylang-input))
                   (reset! @state/write-buffer (full @keylang-input))))
      (handler ev))))

(defn great-white-key-listener [ev]
  (let [pipeline (cond-> identity       
                                        ;time to care

                   
                   #_#_true ((key-handlers (:idx @state/toogleoo)))
                   (= @mode :write) (write-mode-listener)
                   (= @mode :edit) (tree-manip-key-handler)

                   true (mode-switch-listener)
                   true (keystream-watch-listener)
                   true (default-preventer)
                   true (debug-key-listener)
                                        ;keypresses move bottom-up
                   )]
    (pipeline ev)))

(defn refresh-listeners []
  (events/unlisten (.-body js/document)
                   (.-KEYDOWN ^js events/EventType)
                   great-white-key-listener)


  (events/listen (.-body js/document)
                 (.-KEYDOWN ^js events/EventType)
                 great-white-key-listener))
