(ns cas.nat ;parser for two/three-letter codes
  (:require [rum.core :as rum]
            [cas.state :as state :refer [keystream keystream-results keylang-input keystream-tokenized keystream-undecided highlight-atom]]
            [cas.tree-ops :refer [reset-at-path!]]
            [clojure.string :as cs]
))




(rum/defc combos-display < rum/reactive []
  (let [ktoke (rum/react keystream-tokenized)]
    [:div {:style {:text-align "center" :font-size 24} :on-click #(reset! keystream '())}
     [:span {:style { }} (if (seq ktoke)
                           (apply str "tokenized: " (interpose " " ktoke)))]]))


(def letters #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z})


(def num-codes
  #{"on"
    "tw"
    "th"
    "fo"
    "fi"
    "si"
    "se"
    "ei"
    "ni"
    "te"})

(def test-str "ypreqxsqovysq") ;y pr eq x sq ov y sq ;y'=(x^2)/(y^2)
(def cmds  (set (concat
                 num-codes
                 ["eq"
                  "pr"
                  "pl"
                  "mi"
                  "ti"
                  "dby"
                  "sq"
                  "ov"
                  "cl" ;open/close parens
                  "op"]
                 (map (partial str \v) letters))))



(defn create-index [cmds] ;takes arranged as a list of lists lists; by letter, then count

  (reduce (fn [idx word]
            (loop [cursor idx
                   path []
                   remaining-word word]

              (if-let [letter (first remaining-word)]
                (if-let [new-cursor (cursor letter)] ;is there an entry at this cursor for this letter?
                  (recur new-cursor (conj path letter) (rest remaining-word)) ;yes, move to the next
                  (let [final-path (reduce into path (list letter (rest remaining-word)))] ;there's no entry, we've discovered a new word!
                    (assoc-in idx final-path {})))
                idx                ;remaining-word is empty, which means this is shorter than anything that's existed!

                )
              )
            

            ) {} cmds))

(def index (create-index cmds))

(def list-of-cmds-that-are-stubs ; a "stub" is a string that is ambiguously a full command, or the beginning of another command
  (->> cmds
       (filter (fn [cmd]
                 (not= {} (get-in index (apply vector cmd)))))
       (set)))

(defn no-bigger-ones [item]
  (not (list-of-cmds-that-are-stubs item)))

(def digit? #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9})
(def digits? (partial re-matches #"[0-9]+"))
(def numerical? (partial re-matches #"[0-9\.]+"))





                                        ;ambiguity is easy to achieve if you're willing to be heavy handed, e.g. stop-character
                                        ;kinda like the idea of setting vars as vars---but in few keystrokes! literall "s-h", kind of thing

(defn tokenize [s] ;https://en.wikipedia.org/wiki/Lexical_analysis     ;broken and lame, read about lexing
  (let [reducer (fn [[tokens uc] item] ;under-consideration

                  (let [maybe? (str uc item)]
                    
                    (cond (= "" item)
                          (if (not= "" uc)
                            [(conj tokens uc) ""]
                            [tokens uc])
                          
                          (numerical? uc)
                          (cond (not (numerical? item))
                                (recur [(conj tokens uc) ""] item)

                                :else [tokens maybe?])

                          (numerical? item)
                          (cond (and (seq uc)
                                     (not (numerical? uc)))
                                (recur [(conj tokens uc) ""] item)

                                :else [tokens maybe?])
                          
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
    (-> (reduce reducer [[] ""] s)
        (reducer "")
        (first))))



;["w" "sq" "eq" "y" "pr" "mi" "x" "sq"]

;parse-node: {:v v :c []}


(defn idx-of-last [f v]
  (loop [idx (dec (count v))]
    (if (f (v idx))
      idx
      (recur (dec idx)))))


(defn get-type [token]
  (cond
    (#{"op" "("} token)
    :open-paren 
    
    (#{"cl" ")"} token)
    :close-paren

    (or (num-codes token)
        (number? token)
        (and (string? token)
             (numerical? token)))
    :number

    (and (string? token)
         (= (count token) 1))
    :variable

    (#{"eq"} token)
    :comparison

    (#{"pl" "mi"} token)
    :sum

    (#{"ti" "dby" "ov"} token)
    :product

    (#{"pr"} token)
    :prime

    (#{"sq" "exp" "cu"} token)
    :exponent

    (and (str token) (= 1 (count token)))
    :name))

(defn typify [token]
  {:type (get-type token) :value token})

(declare mparse)

(declare precedence)

(def types-data
  ;TODO so making :sum and :product return vectors of pairs was good, but something is glitching because of that, likely because other things aren't ready to get them
  
  {:open-paren {:precedence 1   ;TODO: if left isn't an operator, return a larger [:term] node.

                                        ;caution: paren may have TWO precedences? one as implicit multiplier/fn call, one as grouping?
                ;ANYTHING inside parens has higher precedence than out
                :parselet (fn [[op & remainder]]

                            (println "op: " op)
                            (println "remainder: " remainder)
                            (loop [product [:paren]
                                   rem remainder]
                              (let [item (first rem)]
                                (cond
                                  (#{")" "cl"} item)
                                  {:parsed product
                                   :remaining (rest rem)}
                                  
                                  
                                  :else
                                  (let [{:keys [parsed remaining]}  (mparse rem (precedence :open-paren))]
                                    {:parsed (conj product parsed)
                                     :remaining remaining} ) ))))}

   :comparison {:precedence 2
                :parselet (fn [left [token & remainder :as s]]  
                            (let [{:keys [parsed remaining]} (mparse remainder (precedence :comparison))]
                              {:parsed  [:= left parsed]
                               :remaining remaining}))}
   
   :sum {:precedence 3
         :parselet
         (let [plus (fn [item] [:plus item])
               minus (fn [item] [:minus item])
               gen-sum-pair (fn [op item]
                              ((case op
                                 "pl" plus
                                 "mi" minus)
                               item))]

           (fn [left [op & remainder :as s]]  
             (let [{:keys [parsed remaining]} (mparse remainder (precedence :sum))
                   operands [(plus left) (gen-sum-pair op parsed)]]
               
               (loop [operands operands
                      remaining remaining]
                 (let [lookahead (first remaining)]
                   (if (= :sum (get-type lookahead))
                     (let [{:keys [parsed remaining]} (mparse (rest remaining) (precedence :sum))]
                       (recur (conj operands (gen-sum-pair lookahead parsed))
                              remaining))
                     {:parsed (apply vector :sum operands)
                      :remaining remaining}))))))}
   
   :product  {:precedence 4
              :parselet
              (let [parse #(mparse % (precedence :product))
                    mult (fn [item] [:mult item])
                    dby (fn [item] [:dby item])
                    gen-sum-pair (fn [op item]
                                   ((case op
                                      "ti" mult
                                      "dby" dby
                                      "ov" dby)
                                    item))]

                (fn [left [op & remainder :as s]]  
                  (let [{:keys [parsed remaining]} (mparse remainder (precedence :product))
                        operands [(mult left) (gen-sum-pair op parsed)]]
                    
                    (loop [operands operands
                           remaining remaining]
                      (let [lookahead (first remaining)]
                        (if (= :sum (get-type lookahead))
                          (let [{:keys [parsed remaining]} (mparse (rest remaining) (precedence :product))]
                            (recur (conj operands (gen-sum-pair lookahead parsed))
                                   remaining))
                          {:parsed (apply vector :product operands)
                           :remaining remaining}))))))}

   :exponent {:precedence 5
              :parselet (fn [left [op & remainder :as s]]
                          (cond 
                            (#{"sq" "cu"} op)
                            {:parsed (case op
                                       "sq" [:exp left 2]
                                       "cu" [:exp left 3])
                             :remaining remainder}

                            (= op "exp")
                            (-> remainder
                                (mparse (precedence :exponent))
                                (update :parsed (partial vector op left)))))}

   :prime {:precedence 6
           :parselet (fn [left [op & remainder :as s]]
                       {:parsed [op left]
                        :remaining remainder})}



       
   :variable {:precedence 9
              :parselet (fn [[v & remainder]]
                          {:parsed v
                           :remaining remainder #_(mparse remainder (precedence :variable))})}

   :number {:precedence 9
            :parselet (fn [[token & remainder]]
                        {:parsed (cond
                                   (number? token)
                                   token
                                   
                                   (num-codes token)
                                   (case token
                                     "on" 1 ;should move this stuff to lexing
                                     "tw" 2
                                     "th" 3
                                     "fo" 4
                                     "fi" 5
                                     "si" 6
                                     "se" 7
                                     "ei" 8
                                     "ni" 9
                                     "te" 10)
                                   
                                   (not (string? token))
                                   (println "hi, type is: " (str (type token )))

                                   (re-matches #".*\..*" token)
                                   (js/parseFloat token)

                                   :else (js/parseInt token))
                         :remaining remainder})}})


(defn get-parser [token-or-type]
  (let [typpe (or (and (keyword? token-or-type) token-or-type)
                  (get-type token-or-type))]
    (-> typpe types-data :parselet)))

(defn precedence [token-or-type]
  (let [typpe (or (and (keyword? token-or-type) token-or-type)
                  (get-type token-or-type))]
    (-> typpe types-data :precedence)))

#_(defn precedence2 [type] ;should be typifying tokens and using this everywhere, but...
  (-> typpe types-data :precedence))

#_(defn get-parser2 [type]
  (-> typpe types-data :parselet))


(defn independent? [token]
  (number? token))

;doing some magic here; note that mparse is called recursively by most parsers, AND has an interior loop

(defn mparse [[token & remainder :as s] prec-val]
  (if token
    (let [parser (get-parser token)
          state (parser s)]

      (let [result (loop [{:keys [parsed remaining] :as state} state]
                     (let [lookahead (first remaining)]
                       (if (< prec-val (precedence lookahead))
                            (let [parser (get-parser lookahead)
                                  state (parser parsed remaining)]
                              (recur state))
                            state)))]
        result))))

(defn iparse [token-vec]  ;interface parse
  (-> token-vec
      (mparse 0)
      :parsed))

(defn full [s]
  (-> s tokenize iparse))

(add-watch keylang-input :tokenize (fn [k r o n] (reset! keystream-tokenized (tokenize n))));todo move this somewhere 


["x" "sq" "eq" "y" "sq"] ; the "goal parsed structure" here would be [:eq [:sq \x] [:sq \y]]
"x" \x
[:sq \x]






#_(def str-commands
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


#_(def token-pattern  (re-pattern  (str "(" (apply str  (interpose \| (:strs str-commands))) ")$")))


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


