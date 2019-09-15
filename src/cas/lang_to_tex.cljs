(ns cas.lang-to-tex)


(defn gen-fn-for-commutative-infix [op]
  (fn [& operands]
    (let [total-terms  (-> operands count (* 2) dec)]
      (apply str (take total-terms (interleave operands (repeat op)) )))))

(def fns {:+ (gen-fn-for-commutative-infix "+")
          :* (gen-fn-for-commutative-infix "*")
          :- (fn [f s]
               (str f "-" s))
          :/  (fn [f s]
               (str f "/" s))
          
          :frac (fn [f s]
                  (str "\\frac{" f "}{" s "}"))})

(defn compile-to-tex [form]
  (cond (vector? form)
        (let [results (for [item form]
                        (compile-to-tex item))]
          (apply (first results) (rest results)))

        (number? form)
        form

        (keyword? form)
        (form fns)))
