(ns cas.manipulang
  (:require [clojure.string :as stri]))

(defn- tokenize [s] ;get 

  )

(def f (atom nil))

  (def tdata "234 24")


(def letters
  #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z })
(def digits
  #{\1 \2 \3 \4 \5 \6 \7 \8 \9 \0})

(def math-chars
  #{\! \^}
  
  )

(reset! f
        (fn parse-text-to-manipulang [s]
          (partition-by (partial contains? digits) s)
          
          ))


(def kill-switch (atom nil))
(defn stop! []
  (js/clearTimeout @kill-switch)
  (reset! kill-switch nil))

(defn keep-doing [f]
  (if @kill-switch (stop!))
  (reset! kill-switch (js/setInterval f 3000)))

#_(keep-doing #(println (@f tdata)))
#_(stop!)
