(ns cas.backend.nrepl
  (:require [nrepl.server :refer [start-server stop-server default-handler]]
            [cas.backend.config :refer [config]]
            [cider.nrepl :refer [cider-nrepl-handler]]))


(defonce server (if (config :socket)
                  (start-server :port 50505
                                :bind "0.0.0.0"
                                :handler cider-nrepl-handler)))
