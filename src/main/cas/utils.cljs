(ns cas.utils
  (:require [cas.shorthand]))

(def alert js/alert)
(declare evaluate compile parse simplify)


(def letters #{\a \b \c \d \e \f \g \h \i \j \k \l \m \n \o \p \q \r \s \t \u \v \w \x \y \z})

(def u-letters #{\A \B \C \D \E \F \G \H \I \J \K \L \M \N \O \P \Q \R \S \T \U \V \W \X \Y \Z})

(def digits (range 10))
(def digit-strings (map str digits))

(def operator-strings
  #{\!\^\* \( \) \+ \- \/ \< \> \. \=})

(def text-edit!-keys #{"Backspace" "Delete"})


(let [c (atom 0)]
  (defn key-gen []
    (swap! c inc)
    (-> @c str keyword)))
