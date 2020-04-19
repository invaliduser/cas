(ns cas.state
  (:require [cas.test-data]
            [cas.lang-to-tex :refer [compile-to-tex]]
            ))

(def mode (atom :edit)) ;:edit and :tree for now



(def tree-atom (atom cas.test-data/default-data))
(def tex (atom ""))
(reset! tex (compile-to-tex @tree-atom))

(def was-write-mode-before? (atom false))

(def highlight-atom (atom [0]))
(def show-paths? (atom false))

(def highlight-atom-2 (atom nil))

(def keystream (atom '[]))
(def keystream-tokenized (atom []))
(def keystream-resolved-tokens)

(def keystream-results (atom '[]))
(def keystream-undecided (atom '[]))


(def last-key (atom nil))
(def keylang-input (atom ""))

