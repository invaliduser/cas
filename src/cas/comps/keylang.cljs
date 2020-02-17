(ns cas.comps.keylang
  (:require [rum.core :as rum]
            [cas.state :as state :refer [keystream keystream-results keystream-undecided]]))




(rum/defc key-stream-display < rum/reactive []
  [:div {:style {:text-align "center" :font-size 24} :on-click #(reset! keystream '())}
   [:span "resolved here"]
   [:br]
   [:span {:style { }} (apply str (interpose " " (rum/react keystream)))]
   [:div {:style { }} (apply str "stack:" (interpose " " (rum/react keystream-undecided)))]])


(def holder (atom nil))
(def strs ["pr" "str"])


(def test-pattern  (re-pattern  (str "(" (apply str  (interpose \| strs)) ")$")))

(add-watch keystream :to-undecided (fn [k r o n]
                                     (swap! keystream-undecided conj (last n))
                                     (let [proposed-str (apply str @keystream-undecided)
                                           
                                           pattern-test (re-find test-pattern  proposed-str )]

                                       (println "matched?:"  test-pattern ": " proposed-str ":" pattern-test)
                                       (println pattern-test)
                                       (if pattern-test
                                         (let [match (first pattern-test)
                                               c (count match)]
                                           (println "count:" c)
                                           (swap! keystream-undecided (comp vec (partial drop-last c))))))))




#_(re-find test-pattern "akjdldfpr")
;https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions

;foxsp ofxpon


                                        ;ctrl for force-accept

                                        ;last-expression-as-arg
                                        ;meta keys for apply-to-last and apply-to-next

                                        ;ypexsoys -> y'=x^2/y^2---it's not perfect, but it *is* good, and combined with good editing I think it's actually good enough
                                        ;mini functions, that display where their args will go: fr -> blank square over blank square

                                        ; much as you'd like to perfect this, I think you should "release": do the latex stuff, send it off to Jason/Lorenzo
                                        ;editing too


;multiple layers: reg-ex to turn keystrokes/keyseqs into commands, but commands are context-sensitive

