(ns cas.test-data)

(def default-data
  [[:=
    [:list
     2
     :+
     3
     :+
     4]
    [:list
     999
     :-
     [:paren [:list 988 :+ 2]]]]]
  #_["=" ["+" 2 3 4]
     ["-" 999  ["+" 988 2]]])
