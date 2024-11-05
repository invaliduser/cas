(ns cas.backend.latex
  (:require
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [clojure.core.async :as async :refer [go timeout <!]]
   [ring.util.response]
   [cas.common.latex :as comtex]))

(defn gen-name [] (random-uuid))

(defn to-pdf [{:keys [tex-str
                      dest-dir
                      fname] :as args}]
  (println "got req: " tex-str)
  (let [fname (or fname (str "pdf-" (gen-name)))
        dest-dir (or dest-dir "/tmp")

        tex-fname (str "/tmp/" fname ".tex")

        tex-str (comtex/with-boilerplate tex-str)
        target-name (str dest-dir "/" fname ".pdf")]

    (println "writing tex-str to: " tex-fname)
    (sh/sh "touch" tex-fname)

    (io/copy  tex-str (io/file tex-fname))
    (sh/sh "pdflatex"  "-output-directory" dest-dir tex-fname)
    
    (println "target-name:" target-name)

    (go (<! (timeout 60000))
        (sh/sh "rm"  (str dest-dir "/" fname ".*")))
    target-name))

(def h (atom []))

(defn tex-pdf-route [req]
  (let [target-name (-> req
                        :body-params
                        :tex
                        (#(assoc {} :tex-str %))
                        (to-pdf))
        res {:status 200
             :body (io/input-stream target-name)}]
    (swap! h conj {:in (-> req :body-params) :out [target-name res]})
    res)) 
