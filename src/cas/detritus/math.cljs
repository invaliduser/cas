(ns cas.detritus.math
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as cljstr]))

;I *think* this can be thrown away or at least ignored? Though the idea of reverse-engineering type from structure isn't a bad one...

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
