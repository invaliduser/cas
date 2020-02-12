(ns cas.shorthand)

;macros for convenience
(defmacro pull-in [libname fnsyms]
  `(do
     ~@(for [fnsym fnsyms]
         (do
           `(def ~fnsym (~(symbol (str ".-" fnsym)) ~libname))))))
