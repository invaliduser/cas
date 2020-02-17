(ns cas.state
  (:require [cas.test-data]))

(def mode (atom :edit)) ;:edit and :tree for now


(def tex (atom ""))

(def tree-atom (atom cas.test-data/default-data))

(def was-write-mode-before? (atom false))

(def highlight-atom (atom [0]))
(def show-paths? (atom false))

(def keystream (atom '[]))
(def keystream-results (atom '[]))
(def keystream-undecided (atom '[]))
