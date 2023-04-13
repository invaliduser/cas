(ns cas.comps
  (:require [rum.core :as rum]
            [cas.utils :refer [alert parse]]
            [cas.lang-to-tex]
            [cljs.tools.reader.edn]
            [cas.tex-render]
            [cas.comps.board :as board]
            [cas.views.bench :as bench]
            [cas.views.tree-manip :as tree-manip]
            [cas.state :as state]
            [cas.shorthand :as sh]))





(def views [{:component board/backdrop :name "backdrop"}
            {:component bench/bench-comp :name "bench"}
            {:component tree-manip/tree-manip-harness :name "tree manipulation harness"}])



(rum/defc main-comp < rum/reactive []
  [:div {:height "100%"}
   [:div
    [:input {:type "button" :on-click state/advance! :value "Advance!"}]
    [:span (str "  Current value: " (-> (rum/react state/toogleoo) :idx views :name))]
    [:hr]]
[:div]
   ((->  (rum/react state/toogleoo) :idx views :component))

   #_((views (:idx (rum/react state/toogleoo))))])
