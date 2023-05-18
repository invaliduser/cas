(ns cas.backend.latex
  (:require
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [ring.util.response]
   ))

(defn gen-name [] "ok")


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

(defn to-pdf [tex-str]
  (println "got req: " tex-str)
  (let [fname (gen-name)

        tex-fname (str "/tmp/" fname ".tex")
        tex-str (with-boilerplate tex-str)

        dest-dir "/home/daniel/projects/cas/resources/public"
        target-name (str dest-dir "/" fname ".pdf")]
    (println "writing tex-str to: " tex-fname)
    (sh/sh "touch" tex-fname)
    (io/copy  tex-str (io/file tex-fname))
    (sh/sh "pdflatex"  "-output-directory" dest-dir tex-fname)
    
    (println "target-name:" target-name)
    target-name))


(defn tex-pdf-route [req]
  (-> req
      :body-params
      (to-pdf)
      (ring.util.response/response)))

