(ns cas.chans
  (:require
   [cljs.core.async :refer [chan <! >! go-loop]]))

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

