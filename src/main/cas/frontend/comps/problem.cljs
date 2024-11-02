(ns cas.frontend.comps.problem
  (:require [rum.core :as rum :refer-macros [defc]]
            ["@mui/material" :as mui]
            [cas.lang-to.mathml :refer [render-to-inert-mathml]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; main "workbench" component, the "problem" is the basic abstraction
;;; students are working with.  Has "lines" that can be toggled between.
;;; A line is a string, an active/selected bit of manipulang, or an inert
;;; bit of manipulang
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defc problem [lines]
  [:> mui/Box
   (map (fn [line]
          (cond (vector? line)
                (render-to-inert-mathlml line)
                (string? line)
                line))
        lines)])
