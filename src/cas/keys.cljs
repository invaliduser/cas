(ns cas.keys
  (:require [goog.events :as events]
            [rum.core :as rum]
            [cljs.core.async :refer [chan <! >! go]]
            [cas.chans :refer [key-chan]]
            [cas.state :refer [mode was-write-mode-before? highlight-atom keystream keystream-undecided last-key keylang-input]]
            [cas.tree-ops :refer [reset-at-path! append-at-path! full-reset-at-path!]]
            [cas.manipulang :refer [digits letters]]
            [cas.nat :refer [full]]
            [cas.utils :refer [letters u-letters digit-strings operator-strings text-edit!-keys]]
            [cas.shorthand :as sh])
  (:import [goog.events KeyHandler]
           [goog.events.EventType]))

(def hotkeys-map
  {
   ;"w" :start-write-mode
   ;"e" :start-edit-mode
   "o" :open
   "c" :close
   "h" :children
   "s" :snapshot
   ;"m" :minimize

   "Backspace" :delete
   "p" :surround-with-parens
   "r" :replace-with-authoring
   "d" :treat-as-argument-to ;"doto"
#_#_#_#_#_#_#_#_
   "ArrowRight" :right
   "ArrowLeft" :left
   "ArrowUp" :up
   "ArrowDown" :down
   "ArrowRight" :real-right
   "ArrowLeft" :real-left
   "ArrowUp" :real-up
   "ArrowDown" :real-down
   "i" :integral
   "=" :equal})



(defn tree-manip-key-handler [handler]
  (fn [ev]
    (let [k (.-key ev)]
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v)))
        (js/console.log (str k " pressed, no action associated..."))))))


(rum/defc key-stream-display < rum/reactive []
  [:div {:style {:text-align "center" :font-size 24} :on-click #(do
                                                                  (println "reset keystream")
                                                                  (reset! keystream '()))}
   [:span "resolved here"]
   [:br]
   [:span {:style { }} (apply str (interpose " " (rum/react keystream)))]
   [:div {:style { }} (apply str "stack:" (interpose " " (rum/react keystream-undecided)))]])


(def tokenizeable-keys "All keys that can be tokenized."
  (set (concat u-letters letters digit-strings operator-strings)))

(defn tokenizeable-key-handler [handler atm]
  (fn [ev]
    (let [k (.-key ev)]
      (if (or (text-edit!-keys k)
              (tokenizeable-keys k))
        (do
          (println k)
          (case k
            "Backspace"
            (swap! atm #(.slice % 0 -1))
            (swap! atm str k)))
        (handler ev)))))


;key handlers are like middleware, taking another handler
(def key-handlers [#(tokenizeable-key-handler % (cas.state/atom-map "tokenize-material"))
                   identity               ; bench
                   tree-manip-key-handler ;tree-manip

                   ])

(defn debug-key-listener [handler]
  (fn [ev]
    (println "pressed " (.-key ev) " in " @mode " mode")
    (handler ev)))

(defn mode-switch-listener [handler]
  (fn [ev]
    (if (= (.-key ev) \\)
      (let [new-mode (case @mode :write :edit :edit :write)]
        (if (= new-mode :write)
          (reset! was-write-mode-before? false))
        (reset! mode new-mode)
        (println "setting to " new-mode " mode")))
    (handler ev)))

(defn keystream-watch-listener [handler]
  (fn [ev]
    (let [k (.-key ev)]
      (if (not (#{"ArrowRight" "ArrowLeft" "ArrowUp" "ArrowDown"} k))
        (swap! keystream conj k))
      (handler ev))))

(defn send-to-key-chan-listener [handler]
  (fn [ev]
    (let [k (.-key ev)]
      (if-let [v (hotkeys-map k)]
        (do (js/console.log (name v))
            (go (>! key-chan v))
            (handler ev))
        (js/console.log (str k " pressed, no action associated..."))))))

(defn write-mode-listener [handler] ;we're not sure what this does...
  (fn [ev]
    (let [k (.-key ev)]
      (cond (not (.-ctrlKey ev))
            (if @was-write-mode-before?
              (do (swap! keylang-input str k)
                  (full-reset-at-path! @highlight-atom (full @keylang-input)))
              (reset! was-write-mode-before? true))

            #_(if @was-write-mode-before?
                (append-at-path! @highlight-atom k)
                (do (reset-at-path! @highlight-atom k)
                    (reset! was-write-mode-before? true)))))))


(defn great-white-key-listener [ev]
  (let [pipeline (cond-> identity       ;?
                   true ((key-handlers (:idx @cas.state/toogleoo)))
;                   (= @mode :edit) (send-to-key-chan-listener)
;                   (= @mode :write) (write-mode-listener)
                   true (mode-switch-listener)
                   true (keystream-watch-listener)
                   true (debug-key-listener)
                                        ;keypresses move bottom-up
                   
                   )]
    (pipeline ev)))


(defn key-down-listener [ev]
  (reset! last-key ev)
  (let [k (.-key ev)]

    ((tokenizeable-key-handler identity (cas.state/atom-map "tokenize-material"))
     ev)
    
    (println "pressed " k " in " @mode " mode")
    
    (if (= k \\)
      (let [new-mode (case @mode :write :edit :edit :write)]
        (if (= new-mode :write)
          (reset! was-write-mode-before? false))
        (reset! mode new-mode)
        (println "setting to " new-mode " mode"))

      (do (case @mode
            :edit

            (if-let [v (hotkeys-map k)]
              (do (js/console.log (name v))
                  (go (>! key-chan v)))
              (js/console.log (str k " pressed, no action associated...")))

            :write              ;getting into write mode is a one-time thing,

            :default nil)

          
))))

(defn refresh-listeners []
  (events/unlisten (.-body js/document)
                   (.-KEYDOWN events/EventType)
                   great-white-key-listener
                   #_key-down-listener)

  (events/listen (.-body js/document)
                 (.-KEYDOWN events/EventType)
                 great-white-key-listener
                 #_key-down-listener))
