(ns cas.math
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as cljstr]))

(s/def ::right #(cljstr/includes? (.-type %) "Node"))
(s/def ::left  #(cljstr/includes? (.-type %) "Node"))
(s/def ::equation?  (s/keys :req [::left ::right]) )

(defrecord equation [left right])

(defn tex [{:keys [left right] :as equa}]
  #_(s/conform equa ::equation?)
  (str
   (.toTex left)
   "="
   (.toTex right)))
