(ns cas.rpn
  (:require [cljs.core.async :refer [chan <! >! go]]))

(def stack (atom '()))

(def input-stream (atom '()))
(reset! input-stream '(1 2 "-"))

(def fns {"+" {:arity 2 :rfn +}
          "-" {:arity 2 :rfn -}})

(defn advance-step [] 
  #_(do (println "stack: " (apply str (interpose " " @stack)))
      (println "input-stream " (apply str (interpose " " @input-stream))))
  (let [c (first @input-stream)
        _ (swap! input-stream pop)]
    (let [{:keys [arity rfn] :as f} (fns c)]
      (if (and (fns c)
               (>= (count @stack) arity))
        (swap! stack (fn [st] (let [args (take arity st)]
                                (->> st
                                     (drop arity)
                                     (cons (apply rfn args))))))
        (swap! stack conj c)))))

                                        ;two different things going on here:  playing around with stack-based /rpn, but also with the idea of holding keyboard in put in a buffer and interpreting as a stream

                                        ; alternative is much more specific moding
                                        ; i'm skeptical of the accessibility of the first, even as I want to do it

;intimidated by the second
