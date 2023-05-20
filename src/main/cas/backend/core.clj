(ns cas.backend.core
  (:require
   [ring.adapter.jetty :as jetty]
   [cas.backend.routes]
   [cas.backend.config :refer [config]]
   [cas.backend.nrepl]))

(defonce server (atom nil))

(defn start! []
  (reset! server
          (jetty/run-jetty
           cas.backend.routes/handler
           {:port (:port config)
            :join? false})))

(defn stop! []
  (when @server
    (.stop @server))
  (reset! server nil))

(defn reboot! []
  (stop!)
  (start!))

(comment
  (start!)
  (stop!)
  (reboot!))

(defn -main [& args]
  (start!))
