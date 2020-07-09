(ns cas.nat ;parser for two/three-letter codes
  (:require [rum.core :as rum]
            [cas.state :as state :refer [keystream keystream-results keylang-input keystream-tokenized keystream-undecided highlight-atom]]
            [cas.tree-ops :refer [reset-at-path!]]
            [clojure.string :as cs]
            [cas.shorthand :as sh]))

                                        ;TODO:  make cmds longer than two letteres recognizeable
;make square, prime, etc. function as terms
;ability to parse variables

                                        ;design notes: multiple structures, right?

                                        ; typed -> structured manipulang, manipulang -> latex
                                        ;also, they will be manipulating manipulang

                                        ;exponents: ideally you'd just accept one term: aexp5bexp5->  (a^5) * b^5
                                        ;but with terms it gets more complicated ---that could also be a^(5b)^5
                                        ;i think best you can do is implicitly add a ( and expect them to close it
                                        ;you can use hotkeys perhaps, like :downarrow, not always necessary for them to hit "cl"


;you DO want some term discrimination; 2a2b should split out into 2a * 2b, and 5a(a+b) should work out as [:term [:term 5 "a"] [:paren [:sum [:plus "a"] [:plus "b"]]]]


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

(def var-letters
  (set (map (partial str \v) letters)))

(defn unmark-var [v]
  (second v))

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
                  "cu"
                  "exp"
                  "ov"
                  "cl" ;open/close parens
                  "op"]
                 var-letters)))



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
  (not (get-in index (apply vector item)))

  #_(not (list-of-cmds-that-are-stubs item)))

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
    (keyword? token)
    token
    
    (vector? token)
    (first token)
    
    (#{"op" "("} token)
    :open-paren 
    
    (#{"cl" ")"} token)
    :close-paren


    (var-letters token)
    :variable

    (or (num-codes token)
        (number? token)
        (and (string? token)
             (numerical? token)))
    :number

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

    (and (string? token)
         (= (count token) 1))
    :variable

    (and (str token) (= 1 (count token)))
    :name))

(defn typify [token]
  {:type (get-type token) :value token})

(declare mparse)

(declare precedence)
(declare get-parser)

(defn parse-term [term])


(defn signifies-multiplication? [ppe nt]  ;parsed-previous-exp next-token
  (sh/debug ppe)
  (sh/debug nt)

  (try
    (case ppe
      :number (#{:variable :open-paren } nt)
      :variable (#{:variable :open-paren } nt)
      :paren (#{:variable :number :open-paren } nt)
      :exponent (#{:variable :number :open-paren} nt)
      )
    (catch js/Error e (do (sh/debug ppe)
                          (sh/debug nt)))
    )

  )

(defn finish-as-term [r t remainder] ;result type 
#_#_#_  (sh/debug r)
  (sh/debug t)
  (sh/debug remainder)
  (if (signifies-multiplication? t (get-type (first remainder)))
    ((get-parser :term) r remainder)
    {:parsed r
     :remaining remainder}))


(defn is? [t item]
  (= t (get-type item)))

(def types-data
  ;TODO so making :sum and :product return vectors of pairs was good, but something is glitching because of that, likely because other things aren't ready to get them
  
  {:close-paren {:precedence -1
                 :parselet (fn [& args] #_[left more #_[op & remainder]]
                             (println "close-paren parselet args: " args)
                             (throw "close-paren parselet was called! This should never happen!")
                             #_#_#_(sh/debug left)
                             (sh/debug more)

                             {:parsed left
                              :remaining remainder})}

  ; the trick is to rely on the parser getting bored and putting everything in open-paren

   :open-paren {:precedence 15
                :parselet (fn [[op & remainder]]
#_#_                            (sh/debug op)
                            (sh/debug remainder)


                            (let [{:keys [parsed remaining]} (mparse remainder 0)
                                  [close-paren & remaining] remaining]
                              
                              
                              (finish-as-term [:paren parsed] :paren remaining)))}


   :comparison {:precedence 3
                :parselet (fn [left [token & remainder :as s]]  
                            (let [{:keys [parsed remaining]} (mparse remainder (precedence :comparison))]
                              {:parsed  [:= left parsed]
                               :remaining remaining}))}
   
   :sum {:precedence 4
         :parselet
         (let [parse #(mparse % (precedence :sum))
               plus (fn [item] [:plus item])
               minus (fn [item] [:minus item])
               gen-sum-pair (fn [op item]
                              (case op
                                "pl" (plus item) 
                                "mi" (minus item)))]

           (fn [left [op & remainder :as s]]  
             (let [{:keys [parsed remaining]} (parse remainder)
                   operands [(plus left) (gen-sum-pair op parsed)]]
               
               (loop [operands operands
                      [lookahead :as remaining] remaining]
                 (if (is? :sum lookahead)
                   (let [{:keys [parsed remaining]} (parse (rest remaining))]
                     (recur (conj operands (gen-sum-pair lookahead parsed))
                            remaining))
                   {:parsed (apply vector :sum operands)
                    :remaining remaining})))))}
   
   :product {:precedence 5
              :parselet
              (let [parse #(mparse % (precedence :product))
                    mult (fn [item] [:mult item])
                    dby (fn [item] [:dby item])
                    gen-prod-pair (fn [op item]
                                   (case op
                                     "ti" (mult item)
                                     "dby" (dby item)
                                     "ov" (dby item)))]

                (fn [left [op & remainder :as s]]  
                  (let [{:keys [parsed remaining]} (parse remainder)
                        operands [(mult left) (gen-prod-pair op parsed)]]
                    
                    (loop [operands operands
                           [lookahead :as remaining] remaining]

                      (if (not (is? :product lookahead)) 
                        {:parsed (apply vector :product operands)
                         :remaining remaining}

                        (let [{:keys [parsed remaining]} (parse (rest remaining))]
                          (recur (conj operands (gen-prod-pair lookahead parsed))
                                 remaining))
                        
)))))}

   :term {:precedence 6; should be one higher than :product
          :parselet (let [mk-term (fn [l & ns]
                                    (let [reducer (fn [acc v]
                                                    (if (is? :term v)
                                                      (into acc (subvec v 1))
                                                      (conj acc v)))]
                                      (reduce reducer [:term] (cons l ns))))]

                      (fn [left remainder]  
                        (let [{:keys [parsed remaining]} (mparse remainder (precedence :term))
                              pt (get-type parsed) 

                              pt (if (= pt :term)
                                   (get-type (last parsed))
                                   pt)

                              n? (signifies-multiplication? pt (get-type (first remaining)))]

                          (if n?
                            (recur (mk-term left parsed) remaining)
                            {:parsed (mk-term left parsed)
                             :remaining remaining}))))}


;the term stuff:
   ;numbers should care about what's immediately ahead
; if they run into a var , it becomes a term
; if any of these run into a paren...?
; Nystrom doesn't cover differentiating between a(b) = a*b vs a(b)= apply a to b
;one wonders if this is something you handle at the logic layer instead?
   ; yeah, it has to be.

   ;again, a really good authoring/editing experience trumps relation-awareness
   ;anyway: numbers!
   :number {:precedence 9
            :parselet (let [parse #(mparse % (precedence :number))
                            num-map (zipmap ["on" "tw" "th" "fo" "fi" "si" "se" "ei" "ni" "te"] (range 1 11)) 

                            numerical? (fn [toke]
                                         (or
                                          (= :number (get-type toke))
                                          (= toke ".")))

                            n-string (fn [token]
                                       (cond (number? token)
                                             (str token)

                                             (re-matches #"[0-9]" token)
                                             token

                                             (num-codes token)
                                             (str (num-map token))

                                             (= token ".")
                                             token))
                            numerify (fn [token]
                                       (cond
                                         (number? token)
                                         token
                                         
                                         (num-codes token)
                                         (num-map token)
                                         
                                         (not (string? token))
                                         (println "hi, type is: " (str (type token )))

                                         (re-matches #".*\..*" token)
                                         (js/parseFloat token)

                                         :else (js/parseInt token)))]

                        (fn [[token & remainder]]
                          (let [[further-nums remainder] (split-with numerical? remainder)
                                num-whole (cons token further-nums)
                                
                                n (->> num-whole
                                       (map n-string)
                                       (apply str)
                                       (numerify))]

                            (finish-as-term n :number remainder))))}
   

   :exponent {:precedence 7
              :parselet (fn [left [op & remainder :as s]]
                          (let [{:keys [parsed remaining]}
                                (cond 
                                  (#{"sq" "cu"} op)
                                  {:parsed (case op
                                             "sq" [:exp left 2]
                                             "cu" [:exp left 3])
                                   :remaining remainder}

                                  (= op "exp")
                                  (-> remainder
                                      (mparse 0 #_(precedence :exponent))
                                      (update :parsed (partial vector :exp left))))]

                            (finish-as-term parsed :exponent remaining)))}

   :prime {:precedence 8
           :parselet (fn [left [op & remainder :as s]]
                       {:parsed [:prime left]
                        :remaining remainder})}

       
   :variable {:precedence 9
              :parselet (fn [[v & remainder]]

                          (finish-as-term (unmark-var v) :variable remainder)

                          #_{:parsed 'sfjlk
                           :remaining remainder #_(mparse remainder (precedence :variable))})}

})




(defn get-parser [token-or-type]
  (-> token-or-type get-type types-data :parselet)

  #_(let [typpe (or (and (keyword? token-or-type) token-or-type)
                  (get-type token-or-type))]
    (-> token-or-type get-type types-data :parselet)))

(defn precedence [token-or-type]
  (-> token-or-type get-type types-data :precedence)
  #_(let [typpe (or (and (keyword? token-or-type) token-or-type)
                    (get-type token-or-type))]
      (-> token-or-type get-type types-data :precedence)))

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
          state (parser s)]    ;first, parse "once" according to type of first token

      (loop [{:keys [parsed remaining] :as state} state]  ;then as long as the next operator is higher-precedence, repeatedly parse it first
        (let [[lookahead] remaining]
          (if (> (precedence lookahead) prec-val)                                         
            (recur ((get-parser lookahead) parsed remaining))
            state))))))                                                      ;then be done

(defn iparse [token-vec]  ;interface parse
  (-> token-vec
      (mparse  0 #_(precedence (first token-vec)))
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


