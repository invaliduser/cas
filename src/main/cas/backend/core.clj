(ns cas.backend.core
  (:require
   [ring.adapter.jetty :as jetty]
   [ring.middleware.file :refer [wrap-file]]
   #_[org.httpkit.server :as h]))

(def index-html (slurp "resources/public/index.html"))
(def server (atom nil))

(defn handler [req]
  {:status  200
   :body index-html})

#_(defn start! []
  (h/run-server handle {:port 3001}))
(defn wrapped []
  (-> handler
      (wrap-file "resources/public")))

(defn start! []
  (reset! server (jetty/run-jetty (wrapped) {:port 3001
                                      :join? false})))

(defn stop! []
  (.stop @server))
