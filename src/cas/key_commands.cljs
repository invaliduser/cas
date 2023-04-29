(ns cas.key-commands
  (:require [cas.tree-ops :as to]
            [cas.state :refer [highlight-atom tree-atom]]
            [cas.comps.microsoft-directory-tree :refer [update-at-path!]]))


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


{"list of paredit commands"
 [:open-round
  :close-round
  :wrap-round
  :the-deletes ;but keeps parens balanced
  [:c-d :one-char
   :m-d :one-word
   :backspace :one-char
   :m-del :kill-word
   :c-k :kill
   :c-close-round :slurp
   :splicing :m-up :m-down :m-s
   :split :m-S :join :m-j
   {:navigation :stuff}
   "also loads carried by cursor movement and highlighting"
   ]
  ]
 }



