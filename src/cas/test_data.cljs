(ns cas.test-data)

(def default-data
  [[:=
    [:sum
     2
     :+
     3
     :+
     4]
    [:sum
     999
     :-
     [:paren [:sum 988 :+ 2]]]]]
  #_["=" ["+" 2 3 4]
     ["-" 999  ["+" 988 2]]])
