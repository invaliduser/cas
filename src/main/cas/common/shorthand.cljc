(ns cas.common.shorthand)

;macros for convenience
(defmacro pull-in [libname fnsyms]
  `(do ~@(for [fnsym fnsyms]
          (let [jsified (symbol (str ".-" fnsym))]
            `(def ~fnsym (~jsified js/math))))))

(defmacro works? [a]
  `(println ~a))

(defmacro debug [form]
  `(let [res# ~form]
     (println ~(str form) ": "  (str res#))
     res#))

(defmacro store-debug [sym form]
  `(do
     (defonce ~sym (atom []))
      (let [res# ~form]
        (swap! ~sym conj res#)
        res#)))

(defn print-retain [item]
  (println item)
  item)

(defmacro print->>
  [arg exps]
  ~(->> ~arg
        (interleave exps (repeat print-retain))))

(defmacro print-> [arg exps]
  ~(-> ~arg
       (interleave exps (repeat print-retain))))

#_(defmacro make-functions-of-methods [methodsyms]
  `(do
     ~@(for [ms methodsyms]
         (let [periodized (->> ms
                               (str ".")
                               symbol)])
         `(defn ~ms [& args]
            (apply ~periodized )
            )
            )))


(defmacro dt [& terms]
  (->> terms
       first
       str
       (str ".")
       symbol
       (conj (rest terms))))
; i don't really remember what the goal of this is lol...I think I can just add the "." myself...maybe polymorphism?


