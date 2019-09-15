(ns cas.shorthand)


;macros for convenience
(defmacro pull-in [libname [fnsyms]]
  (for [fnsym fnsyms]
    (let [jsified (symbol (str ".-" fnsym))])
    `(def ~fnsym (jsified js/math))))
