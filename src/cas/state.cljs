(ns cas.state
  (:require [cas.test-data])
  )

(def mode (atom :edit)) ;:edit and :tree for now


(def tex (atom ""))

(def tree-atom (atom cas.test-data/default-data))
