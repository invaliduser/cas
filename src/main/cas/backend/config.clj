(ns cas.backend.config
  (:require [aero.core :as aero]))


(def profile (let [hostname (slurp (.getInputStream  (.exec (Runtime/getRuntime)  "hostname")))]
               (if (= hostname "sampo\n")
                 :dev
                 :prod)))

(def config 
  (aero/read-config "config.edn" {:profile profile}))

