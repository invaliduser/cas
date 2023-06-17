(ns cas.frontend.chans
  (:require
   [ajax.core :as ajax]
   [cljs.core.async :refer [chan <! >! go-loop]]
   [cljs.core.async.interop :refer-macros [<p!]]))

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

(comment (api {:uri "/api/latex-compile"
               :params @state/tex}))

(comment
  (defn binary2pdf [binary]
    (<p! (.getDocument (.-pdfjsLib js/window)
                       #js {:data binary})))

  (defn get-binary [tex]
    (<p! (js/Promise. (fn [res rej]
                       (ajax/POST "/api/latex-compile" {:params tex
                                                        :handler res})))))

  (defn render-pdf [pdf]
    (let [page (<p! (.getPage pdf 1))
          context :hmmm
          viewport :hmmm2
          render-context #js {:canvasContext context
                              :viewport viewport}
          render-task (.render page render-context)
          rtp (.-promise render-task)])))

(comment
  (require '[cas.frontend.state :as state])
  (ajax/POST "/api/latex-compile" {:params @state/tex
                                   :handler println
                                   #_#_:body 24})) 
#_(let [backend-chan (chan)]
  (go
    (<! backend-chan))
  )


