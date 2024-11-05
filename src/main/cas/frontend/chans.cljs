(ns cas.frontend.chans
  (:require
   [ajax.core :as ajax :refer [GET POST]]
   [cljs.core.async :refer [chan <! >! go-loop go]]
   [cljs.core.async.interop :refer-macros [<p!]]))

;;probably want as little logic as possible in here, this should literally just be
                                        ;(def a-chan (chan)), (def b-chan (chan)), etc
; but for now


(def key-chan (chan))

(defn action-interpreter [nom m c & {:keys [before after]}] ;name map chan 
  (println (str "starting " nom " listener..."))
  (go-loop []
    (let [e (<! c)]
      (if before (before e)) 
      (if-let [action (m e)]
        (do (action)
            (after e))
        (do (throw (js/Error.  (str "action-interpreter " nom " got signal of " e ", but no action registered." ))))))
      (recur)))



(defn api [opts] ;:uri :method :format :response-format :handler :params :headers :cookie-policy
  (ajax/ajax-request opts))

(def h (atom nil))

(def pdf-chan (chan))

(defn tex2pdf [tex]
  (POST  "/api/latex-compile"
         {:params {:tex tex}
          :response-format (assoc (ajax/raw-response-format) :type :arraybuffer)
          :handler #(go (>! pdf-chan  (js/Uint8Array. %)))}))

(comment
  (tex2pdf "2+3x-4y=20"))
