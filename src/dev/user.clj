(ns user
  (:require
   [cas.backend.core :as app-server]
   [shadow.cljs.devtools.api :as shadow]
   [shadow.cljs.devtools.server :as shadow-server]))


(defn cljs []
  (when (nil? @app-server/server)
    (app-server/start!))
  (shadow-server/start!)
  (shadow/watch :app)
  (shadow/nrepl-select :app))

(def stop! shadow-server/stop!)
