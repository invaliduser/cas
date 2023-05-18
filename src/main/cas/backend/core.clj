(ns cas.backend.core
  (:require
   [ring.adapter.jetty :as jetty]
   [cas.backend.routes]
   #_[org.httpkit.server :as h]))

(def index-html (slurp "resources/public/index.html"))
(defonce server (atom nil))

#_(defn handler [req]
  {:status  200
   :body index-html})

#_(defn start! []
  (h/run-server handle {:port 3001}))

(defn start! []
  (reset! server (jetty/run-jetty cas.backend.routes/handler {:port 3001
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
