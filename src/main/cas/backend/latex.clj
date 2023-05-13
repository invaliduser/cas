(ns cas.backend.latex
  (:require
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]))

(defn gen-name [] "ok")

(defn to-pdf [tex-str]
  (let [fname (gen-name)
        fname (str fname ".tex")
        path "/tmp/"
        full-fname (str path fname)
        file (io/file full-fname)]
    (println "writing tex-str to: " full-fname)
    (sh/sh "touch" full-fname)
    (io/copy tex-str file)
    (sh/sh "pdflatex"  "-output-directory" "/home/daniel/projects/cas/resources/public" full-fname)))
