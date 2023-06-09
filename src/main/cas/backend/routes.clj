(ns cas.backend.routes
  (:require [reitit.core :as r]
            [reitit.ring :as rr]
            [cas.backend.latex :as latex]
            [ring.middleware.file :refer [wrap-file]]
            [ring.util.request :as rur]
            [muuntaja.middleware :as muuntaja]
            [muuntaja.core :as m]))


(def index-html (slurp "resources/public/index.html"))

(defn default-handler [req]
  {:status  200
   :body index-html})

(def routes ["/" 
             ["api"
              ["/latex-compile" {:post latex/tex-pdf-route} ]]])

(def router (rr/router routes))

(def route-handler
  (rr/ring-handler
   router
   default-handler))

(defn wrap-print-req-body [handler id-str]
  (fn [req]
    (println (str id-str ": " (:body req)))
    (handler req)))

(defn wrap-body-string [handler]
  (fn [req]
    (-> req
        (assoc :body (rur/body-string req))
        handler)))

(def req-resp? (atom true))
(def req-resp (atom []))
(defn wrap-debug-store [handler]
  (fn [req]
    (let [resp (handler req)]
      (when @req-resp?
        (swap! req-resp conj {:req req :resp resp}))
      resp)))

(def handler
  (-> route-handler      ;requests  go bottom up, responses go top down
      (wrap-debug-store)
      (muuntaja/wrap-format)
      (wrap-body-string)
      (wrap-file "resources/public")))

