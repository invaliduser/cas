(ns cas.comps.keylang
  (:require [rum.core :as rum]
            [cas.state :as state :refer [keystream keystream-results keystream-undecided highlight-atom]]
            [cas.tree-ops :refer [reset-at-path!]]
            [clojure.string :as cs]
            [instaparse.core :as insta]))

     (def math? ;this works but is dumb, and instaparse doesn't mix well w/our tokens.  see https://en.wikipedia.org/wiki/Operator-precedence_parser
       (insta/parser
        "comparison-exp = ((additive-exp ((comparison-operator) additive-exp)*) | (mult-exp ((comparison-operator) mult-exp)*) | (exp-exp ((comparison-operator) exp-exp)*))
     additive-exp = mult-exp (('+' | '-') mult-exp)*
     mult-exp = exp-exp (('*' | '/') exp-exp)*
     comparison-operator = ('>'|'>='|'<'|'<='|'=')
     parens = '(' additive-exp ')'
     exp-exp = term ['^' term]
     term = parens
     term = number? (letter | parens)*
     letter = #'[a-zA-Z]'
     number = #'[0-9]+'"))
(def term-parse
  (insta/parser
   "term= number letter
    number = #'[0-9]+'
    letter = #'[a-zA-Z]'"))

(rum/defc key-stream-display < rum/reactive []
  [:div {:style {:text-align "center" :font-size 24} :on-click #(reset! keystream '())}
   [:span "resolved here"]
   [:br]
   [:span {:style { }} (apply str (interpose " " (rum/react keystream)))]
   [:div {:style { }} (apply str "stack:" (interpose " " (rum/react keystream-undecided)))]])


(def holder (atom nil))
(def strs ["pr" "str"])


(def letters #{\a \b \c \d \e \f \g \h \i \j \k \l \m  \o \p \q \r \s \t \u \v \w \x \y \z})

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
                        "ti"
                        "dby"
                        "sq"
                        "ov"
                        "cl"
                        "op"
                        ]
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


(defn idx-of-last [f v]
  (loop [idx (dec (count v))]
    (if (f (v idx))
      idx
      (recur (dec idx)))))


(defn get-type [token]
  (cond (number? token)
        :number

        (#{"pl" "mi"} token)
        :sum

        (#{"ti" "dby" "ov"} token)
        :product

        (#{"pr"} token)
        :prime

        (#{"sq" "exp" "cu"} token)
        :exponent

        (and (str token) (= 1 (count token)))
        :name
        ))


(def types-data
  ;TODO so making :sum and :product return vectors of pairs was good, but something is glitching because of that, likely because other things aren't ready to get them

  
  {:number {:precedence nil
            :parselet (fn [[op & remainder]]
                        {:parsed op
                         :remaining remainder})}
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
                     {:parsed (apply vector "sum" operands)
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
                          {:parsed (apply vector "product" operands)
                           :remaining remaining}))))))}

   :exponent {:precedence 5
              :parselet (fn [left [op & remainder :as s]]
                          (cond 
                            (#{"sq" "cu"} op)
                            {:parsed [op left]
                             :remaining remainder}

                            (= op "exp")
                            (-> remainder
                                (mparse (precedence :exponent))
                                (update :parsed (partial vector op left)))))}

   :prime {:precedence 6
           :parselet (fn [left [op & remainder :as s]]
                       {:parsed [op left]
                        :remaining remainder})}
   :paren {:precedence 7 ; this is prolly why you need two tables...
           :parselet (fn [] nil)}

                                        ;this actually suffers from quite a bit of ambiguity
                                        ;consider a(b+c).  Is this a function call, or multiplication?
                                        ;in math this is known by context, but...
                                        ;the nice thing is we don't have to care; we can make up our own operation, adjacent-and-parenthesized
                                        ;the actual reason we care is these two ops: "of" and "ti" "po" (times open paren)
                                        ;similarly we should beware treating +-*/ as binary ops.  for the purposes of math notation, they're of unlimited arity, i.e. 2 + 3 + 4 is not a two-level nested expression, (2+(3+4)), as you're currently parsing it, but rather (+ 2 3 4)

                                        ;LOWER precedence is what breaks it. What breaks 2*3*4*5... is the inclusion of a lower-precedence op like +
   
   })


(defn get-parser [token-or-type]
  (let [typpe (or (and (keyword? token-or-type) token-or-type)
                  (get-type token-or-type))]
    (-> typpe types-data :parselet)))

(defn precedence [token-or-type]
  (let [typpe (or (and (keyword? token-or-type) token-or-type)
                  (get-type token-or-type))]
    (-> typpe types-data :precedence)))


(declare mparse)

(defn independent? [token]
  (number? token))

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




["x" "sq" "eq" "y" "sq"] ; the "goal parsed structure" here would be [:eq [:sq \x] [:sq \y]]
"x" \x
[:sq \x]




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


