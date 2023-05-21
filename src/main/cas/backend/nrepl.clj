(ns cas.backend.nrepl
  (:require [nrepl.server :refer [start-server stop-server default-handler]]
            [cas.backend.config :refer [config]]
            [cider.nrepl :refer [cider-nrepl-handler]]))


(defonce server (if-let [socket (config :socket)]
                  (start-server :port (socket :port )
                                :bind (socket :bind)
                                :handler cider-nrepl-handler)))
