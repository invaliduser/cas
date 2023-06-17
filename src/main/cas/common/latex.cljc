(ns cas.common.latex)

(defn add-preamble  [s]
  (str "\\documentclass{article}" s))
(defn wrap-document [s]
  (str "\\begin{document}"
       s
       "\\end{document}"))

(defn with-boilerplate [tex-str]
  (-> tex-str
      (wrap-document)
      (add-preamble)))
