(ns cas.lang-to-tex
  (:require [clojure.string :as stri])
  )


(defn gen-fn-for-commutative-infix [op]
  (fn [& operands]
    (let [total-terms  (-> operands count (* 2) dec)]
      (apply str (take total-terms (interleave operands (repeat op)) )))))

(def fns {:= (fn [f s] (str f "=" s))

          :paren (fn [item] (str "(" item ")"))
          :plus (fn [item] (str "+" item))
          :minus (fn [item] (str "-" item))
          :sum (fn [uno & others]
                 (apply str (if (= (first uno) "+")
                              (stri/replace-first uno #"\+" "")
                              uno)
                        others))

                    

          
                                        ;          :+ (gen-fn-for-commutative-infix "+")
;          :* (gen-fn-for-commutative-infix "*")
          :- (fn [f s]
               (str f "-" s))
          :/  (fn [f s]
               (str f "/" s))
          :exp (fn [f s] (str f "^{" s"}"))
          :frac (fn [f s]
                  (str "\\frac{" f "}{" s "}"))
          :prime (fn [item] (str item "^{\\prime}"))
          })

(def nkw-fns (zipmap (map name (keys fns)) (vals fns)))

(def operator?
  (set (keys fns)))

(defn compile-to-tex [form]
  (cond (vector? form)
        (let [results (for [item form]
                        (compile-to-tex item))]
          (apply (first results) (rest results)))

        (number? form)
        form

        (operator? form)
        (fns form)

        (string? form)
        (cond #_(= form "cursor")
              
              
              :else (let [inted (js/parseInt form)]
                      (if (js/isNaN inted)
                        form
                        inted)))))

