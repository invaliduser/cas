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
   "s" :snapshot
   ;"m" :minimize

   "Backspace" :delete
   "p" :surround-with-parens
   "r" :replace-with-authoring
   "d" :treat-as-argument-to ;"doto"
   "ArrowRight" :right
   "ArrowLeft" :left
   "ArrowUp" :up
   "ArrowDown" :down
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
  [:div {:style {:text-align "center" :font-size 24} :on-click #(reset! keystream '())}
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


(def key-handlers [#(tokenizeable-key-handler % (cas.state/atom-map "tokenize-material"))
                   identity ; bench
                   tree-manip-key-handler ;tree-manip

                   ])

(defn great-white-key-listener [ev]
  (let [pipeline (cond-> identity ;?
                   true ((key-handlers (:idx @cas.state/toogleoo)))
                                        ;keypresses move bottom-up
                   )]
    (pipeline ev)))


(defn key-down-listener [ev]
  (reset! last-key ev)
  (let [k (.-key ev)]

    ((tokenizeable-key-handler identity (cas.state/atom-map "tokenize-material"))
     ev)
#_#_    
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
            (cond (not (.-ctrlKey ev))
                  (if @was-write-mode-before?
                    (do (swap! keylang-input str k)
                        (full-reset-at-path! @highlight-atom (full @keylang-input)))
                    (reset! was-write-mode-before? true))

                  #_(if @was-write-mode-before?
                      (append-at-path! @highlight-atom k)
                      (do (reset-at-path! @highlight-atom k)
                          (reset! was-write-mode-before? true))))
            :default nil)

          
          (if (not (#{"ArrowRight" "ArrowLeft" "ArrowUp" "ArrowDown"} k))
            (swap! keystream conj k))))))

(defn refresh-listeners []
  (events/unlisten (.-body js/document)
                   (.-KEYDOWN events/EventType)
                   #_great-white-key-listener
                   #_key-down-listener)

  (events/listen (.-body js/document)
                 (.-KEYDOWN events/EventType)
                 great-white-key-listener
                 #_key-down-listener))
