(ns cas.lang-to-tex
  (:require [clojure.string :as stri])
  )
;http://manuels.github.io/texlive.js/

(defn gen-fn-for-commutative-infix [op]
  (fn [& operands]
    (let [total-terms  (-> operands count (* 2) dec)]
      (apply str (take total-terms (interleave operands (repeat op)) )))))

(def fns
  (let [frac-f  (fn [f s]
                  (str "\\frac{" f "}{" s "}"))]
    {:= (fn [f s] (str f "=" s))

     :paren (fn [item] (str "(" item ")"))
     :plus (fn [item] (str "+" item))
     :minus (fn [item] (str "-" item))
     :sum (fn [& items]
            (apply str items))
     :+ "+"
     :- "-"
     :mult (fn [item] (str "*" item))

     :dby (fn [item] (str "*\\frac{1}{" item "}"))
     
     :product (fn [uno & others]
            (apply str (if (= (first uno) "*")
                         (stri/replace-first uno #"\*" "")
                         uno)
                   others))
;much work to be done here
     

     
                                        ;          :+ (gen-fn-for-commutative-infix "+")
                                        ;          :* (gen-fn-for-commutative-infix "*")
     :/  frac-f
#_#_     :dby frac-f  ;these should be one; users will see it
     :ov frac-f
     :exp (fn [f s] (str f "^{" s"}"))
     :frac (fn [f s]
             (str "\\frac{" f "}{" s "}"))
     :prime (fn [item] (str item "^{\\prime}"))
     :term (fn [& items] (apply str items))}))


(def nkw-fns (zipmap (map name (keys fns)) (vals fns)))


(def operator?
  (set (keys fns)))

(defn- compile-to-tex* [form]
  (cond (vector? form)
        (let [results (for [item form]
                        (compile-to-tex* item))]
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

(defn compile-to-tex [form]
  (str (compile-to-tex* form)))

