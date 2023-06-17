(ns cas.frontend.tex
  (:require [cas.lang-to.tex :as lang]))

(defn problems-to-tex [problems]
  (->> problems
       (map first)
       (map lang/compile-to-tex)))
