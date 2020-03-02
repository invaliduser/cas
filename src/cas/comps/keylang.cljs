(ns cas.comps.keylang
  (:require [rum.core :as rum]
            [cas.state :as state :refer [keystream keystream-results keystream-undecided highlight-atom]]
            [cas.tree-ops :refer [reset-at-path!]]
            [clojure.string :as cs]
            ))




(rum/defc key-stream-display < rum/reactive []
  [:div {:style {:text-align "center" :font-size 24} :on-click #(reset! keystream '())}
   [:span "resolved here"]
   [:br]
   [:span {:style { }} (apply str (interpose " " (rum/react keystream)))]
   [:div {:style { }} (apply str "stack:" (interpose " " (rum/react keystream-undecided)))]])


(def holder (atom nil))
(def strs ["pr" "str"])


(def letters [\a \b \c \d \e \f \g \h \i \j \k \l \m  \o \p \q \r \s \t \u \v \w \x \y \z])

(def str-commands
  {:fn (fn [s]
         (cond
           (re-find #"^[a-z]$" s)
           (reset-at-path! @highlight-atom s)
           )
         )
   :strs (concat
          ["eq"
           "pr"
           "pl"
           "mi"
           "sq"
           "ov"
           "cl"]
          (map str letters))}  )



(def token-pattern  (re-pattern  (str "(" (apply str  (interpose \| (:strs str-commands))) ")$")))


(def test-str "ypreqxsqovysq") ;y pr eq x sq ov y sq ;y'=(x^2)/(y^2)
(def cmds  (set (concat
                       ["eq"
                        "pr"
                        "pl"
                        "mi"
                        "sq"
                        "ov"
                        "cl"]
                       (map str letters))))

(def list-of-cmds-that-are-stubs
  (let [arranged (->> cmds
                      (apply list)
                      (sort)
                      (group-by first)
                      vals
                      (filter #(> (count %) 1))
                      (map (partial sort #(< (count %) (count %2)))))
        _ (println "arranged: " arranged)

        shorters-of-group (fn [grp]
                            (let [grpd (partition-by count grp)

                                  reducer (fn [[wtas wos] wg] ;words that are stubs ; words one shorter ; current-count ;word-group
                                            (if wos
                                              (let [culprits (->> wg
                                                                  (filter (fn [possible-longer]
                                                                            (re-find
                                                                             (re-pattern (apply str "^(" (cs/join "|" wos) ")."))
                                                                             possible-longer)))
                                                                  
                                                                  #_(map #(println "after distinct, before drop-last: " %))
                                                                  (map (comp (partial apply str) drop-last))
                                                                  (distinct))]                                                
                                                [(concat wtas culprits) wg])
                                              [wtas wg]))]
                              (reduce reducer [[] nil]  grpd)))]
    (->> arranged
         (map shorters-of-group)
         (map first)
         (apply concat)
         set)))

(defn no-bigger-ones [item]
  (not (list-of-cmds-that-are-stubs item)))

(defn tokenize [s]
  (let [reducer (fn [[tokens uc] item] ;under-consideration
                  (let [maybe? (str uc item)]
                    (cond
                      (= "z" item) ;should be STOP-CHARACTER
                      [(conj tokens uc) ""]

                      (and (no-bigger-ones maybe?)
                           (cmds maybe?))
                      [(conj tokens maybe?) ""]

                      (and (not (cmds maybe?))
                           (no-bigger-ones maybe?)
                           (seq uc))
                      [(conj tokens uc) (str item)]

                      :else [tokens maybe?])))]
    (first (reduce reducer [[] ""] s))))



;["w" "sq" "eq" "y" "pr" "mi" "x" "sq"]

;parse-node: {:v v :c []}
(defn parse [tokens]
  (let [reducer (fn [{:keys [tree stack cursor]} item]
                  (cond (letters item)
                        {}
                        ) 
                  )]
    (reductions reducer {:tree nil :stack [] :cursor nil} tokens)
    )

  )



#_(add-watch keystream :to-undecided (fn [k r o n]
                                     (swap! keystream-undecided conj (last n))
                                     (let [proposed-str (apply str @keystream-undecided)
                                           
                                           pattern-test (re-find token-pattern  proposed-str )]

                                       (println "matched?:"  token-pattern ": " proposed-str ":" pattern-test)
                                       (println pattern-test)
                                       (if pattern-test
                                         (let [match (first pattern-test)
                                               c (count match)]
                                           (println "match:" match)
                                           ((:fn str-commands) match)
                                           (println "count:" c)
                                           (swap! keystream-undecided (comp vec (partial drop-last c))))))))




#_(re-find token-pattern "akjdldfpr")
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


