(ns cas.key-commands
  (:require [cas.tree-ops :as to]
            [cas.state :refer [highlight-atom tree-atom]]
            [cas.tree-ops :refer [update-at-path!]]
            ))


;AUTHORING
(defn set-equal-to []
  (swap! tree-atom (fn [tree]
                     ["="  tree]))    ;TODO: to move correctly, asses type of top sym ;
                                        ;move top level one down
                                        ;put an eq at that level
                                        ;move cursor to other side
)


(defn do-to-expr [path outer]   ;wraps node in something, expects fake-path
  (update-at-path! path (fn [node]
                          [outer node])))

