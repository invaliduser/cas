(ns cas.shorthand)


;macros for convenience
(defmacro pull-in [libname fnsyms]
  `(do ~@(for [fnsym fnsyms]
          (let [jsified (symbol (str ".-" fnsym))]
            `(def ~fnsym (~jsified js/math))))))


(defn print-retain [item]
  (println item)
  item)

(defmacro thread-print [arg exps]
  ~(->> ~arg
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


(defmacro dt [ & terms]
  (->> terms
       first
       str
       (str ".")
       symbol
       (conj (rest terms))))
; i don't really remember what the goal of this is lol...I think I can just add the "." myself...maybe polymorphism?
