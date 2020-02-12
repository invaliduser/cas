(ns cas.lang-to-tex)


(defn gen-fn-for-commutative-infix [op]
  (fn [& operands]
    (let [total-terms  (-> operands count (* 2) dec)]
      (apply str (take total-terms (interleave operands (repeat op)) )))))

(def fns {:= (fn [f s] (str f "=" s))
          :+ (gen-fn-for-commutative-infix "+")
          :* (gen-fn-for-commutative-infix "*")
          :- (fn [f s]
               (str f "-" s))
          :/  (fn [f s]
               (str f "/" s))
          
          :frac (fn [f s]
                  (str "\\frac{" f "}{" s "}"))})

(def nkw-fns (zipmap (map name (keys fns)) (vals fns)))

(def operator?
  #{\+
    \=
    \-
    \*
    \/})

(defn compile-to-tex-legacy [form] ;only takes operators in keyword form, could maybe be changed
  (cond (vector? form)
        (let [results (for [item form]
                        (compile-to-tex-legacy item))]
          (apply (first results) (rest results)))

        (number? form)
        form

        (keyword? form)
        (fns form)))


(defn compile-to-tex [form] ;deprecated? relies on 
  (cond (vector? form)
        (let [results (for [item form]
                        (compile-to-tex item))]
          (apply (first results) (rest results)))

        (number? form)
        form

        (operator? form)
        (nkw-fns form)))

