(ns cas.tex-render)


(defn render-tex [tex]
  (.tex2svg js/MathJax tex))
