(ns cas.test-data)

(def default-data
  [:=
   [:sum
    [:plus 2]
    [:plus 3]
    [:plus 4]]
   [:sum
    [:plus 999]
    [:minus [:paren [:sum [:plus 988] [:plus 2]]]]]]
  #_["=" ["+" 2 3 4]
     ["-" 999  ["+" 988 2]]])
