
;Here we're working on specs for the MANIPULATION format.

;the point is that these are *checks*, not so much an object hierarchy

(ns cas.structs
  (:require [clojure.spec.alpha :as s]
           [cas.utils :as utils]))

(s/def ::number number?)
(s/def ::el-operator #{:+ :- :* :/})

(s/def ::varble (set (map keyword utils/letters)))


(s/def ::atomic-term (s/or :number ::number
                           :variable ::varble))


(s/def ::complex-variable (s/tuple ::varble identity))

(s/def ::short-term (s/cat :coeff (s/? ::number)
                           :vars (s/* ::varble))) ;change to accommodate exps


(s/def ::frac (s/tuple :frac ::expr ::expr))


;the issue right now is the circular relationship between terms, parens, and expressions


;;;;;;seems like using regex here can create great problems

;;;;;methin


;;wondering if I just put the raw regex in there it would work...
;use spec to verify type, not detect

;======================================================
;!!!!!!"feels like an expression tree, looks like latex"!!!!!!
;======================================================
;;everything they "type in" needs to be parsed
;; but they can still move around the tree
;;we do not show them the tree!
;; they get a feel for it.
;; which means we do have that nice, overly-ceremonial tree

;; meanwhile we retain the :product and :sum structures
;; 


;; there should be very little computation here!
(s/def ::chain (s/or :product ::product
                     :sum ::sum))   ; chain may replace product sum


(s/def ::expr (s/or :paren ::paren
                    :term ::term
                    :chain ::chain))


                                        ;a paren is [:paren :expr]
;a chain is a series of terms or parens interleaved by dyadic ops
;a term is one or more things that are terms, in sequence







