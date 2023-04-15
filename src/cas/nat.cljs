(ns cas.nat
  #_(:require
   #_[cas.utils :refer [letters]]
   #_[shorthand :as sh]))

(def letters #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z})

(def num-map
  (zipmap
   ["ze" "on" "tw" "th" "fo" "fi" "si" "se" "ei" "ni" "te"] (range 0 11)))

(def num-codes
  (set (keys num-map)))

(def var-letters
  (set (map (partial str \v) letters)))

(def test-str "ypreqxsqovysq") ;y pr eq x sq ov y sq ;y'=(x^2)/(y^2)
(def cmds  (set (concat
                 num-codes
                 ["eq"
                  "lt" "gt" "leq" "geq"
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
                 letters)))



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
                                        ;kinda like the idea of setting vars as vars---but in few keystrokes! literally "s-h", kind of thing

(defn tokenize [s]
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
                          
                          (= "\\" item) ;should be STOP-CHARACTER
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


    (letters token)
    :variable

    (or (num-codes token)
        (number? token)
        (and (string? token)
             (numerical? token)))
    :number

    (#{"eq" "gt" "lt" "leq" "geq"} token)
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


(defn signifies-multiplication? [ppe nt]  ;parsed-previous-exp next-token
  (case ppe
      :number (#{:variable :open-paren } nt)
      :variable (#{:variable :open-paren } nt)
      :paren (#{:variable :number :open-paren } nt)
      :exponent (#{:variable :open-paren} nt)
      :exp (#{:variable :open-paren} nt)
      :prime (#{:variable :open-paren} nt)))

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
  {:close-paren {:precedence -1
                 :parselet (fn [& args] #_[left more #_[op & remainder]]
                             (println "close-paren parselet args: " args)
                             (throw "close-paren parselet was called! This should never happen!")
                             #_#_#_(sh/debug left)
                             (sh/debug more)

                             {:parsed left
                              :remaining remainder})}

   :open-paren {:precedence 15
                :parselet (fn [[op & remainder]]
#_#_                            (sh/debug op)
                            (sh/debug remainder)

                            (let [{:keys [parsed remaining]} (mparse remainder 0)
                                  [close-paren & remaining] remaining]
                              
                              
                              (finish-as-term [:paren parsed] :paren remaining)))}


   :comparison {:precedence 3
                :parselet (fn [left [token & remainder :as s]]  
                            (let [comparator ({"geq" :>=
                                               "leq" :<=
                                               "gt"  :>
                                               "lt"  :<
                                               "eq"  :=} token)

                                  {:keys [parsed remaining]} (mparse remainder (precedence :comparison))]
                              {:parsed  [comparator left parsed]
                               :remaining remaining}))}
   
   :sum {:precedence 4
         :parselet
         (let [parse #(mparse % (precedence :sum))
               get-sign {"pl" :+ "mi" :-}]
           (fn [left [op & remainder :as s]]  
             (let [{:keys [parsed remaining]} (parse remainder)  
                   items [left (get-sign op) parsed]]
               (loop [items items
                      [lookahead :as remaining] remaining]
                 (if (is? :sum lookahead)
                   (let [{:keys [parsed remaining]} (parse (rest remaining))]
                     (recur (into items [(get-sign lookahead) parsed])
                            remaining))
                   {:parsed (apply vector :sum items)
                    :remaining remaining})))))}
   
   :product {:precedence 5
             :parselet (let [parse #(mparse % (precedence :product))
                             get-sign (fn [op]
                                        (case op
                                          "ti" :*
                                          "dby" :/
                                          "ov" :/ ))]

                         (fn [left [op & remainder :as s]]  
                           (let [{:keys [parsed remaining]} (parse remainder)
                                 operands [left (get-sign op) parsed]]
                             
                             (loop [operands operands
                                    [lookahead :as remaining] remaining]

                               (if (not (is? :product lookahead)) 
                                 {:parsed (apply vector :product operands)
                                  :remaining remaining}

                                 (let [{:keys [parsed remaining]} (parse (rest remaining))]
                                   (recur (into operands [(get-sign lookahead) parsed])
                                          remaining)))))))}

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

                              n? (try (signifies-multiplication? pt (get-type (first remaining)))
                                      (catch js/Error e (println e "and: " parsed "and: " remaining))
                                      )]

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
                            numerical? (fn [toke]
                                         (= :number (get-type toke)))

                            n-string (fn [token]
                                       (cond (number? token)
                                             (str token)

                                             (re-matches #"[0-9]+" token)
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
                       (finish-as-term  [:prime left] :prime remainder)

                       #_{:parsed [:prime left]
                        :remaining remainder})}


   
   :variable {:precedence 9
              :parselet (fn [[v & remainder]]

                          (finish-as-term v :variable remainder)

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
  (println "running mparse!" s prec-val)
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

;todo move this somewhere 


["x" "sq" "eq" "y" "sq"] ; the "goal parsed structure" here would be [:eq [:sq \x] [:sq \y]]
"x" \x
[:sq \x]







;foxsp ofxpon


                                        ;ctrl for force-accept

                                        ;last-expression-as-arg
                                        ;meta keys for apply-to-last and apply-to-next

                                        ;ypexsoys -> y'=x^2/y^2---it's not perfect, but it *is* good, and combined with good editing I think it's actually good enough
                                        ;mini functions, that display where their args will go: fr -> blank square over blank square

                                        ; much as you'd like to perfect this, I think you should "release": do the latex stuff, send it off to Jason/Lorenzo
                                        ;editing too


;multiple layers: reg-ex to turn keystrokes/keyseqs into commands, but commands are context-sensitive

