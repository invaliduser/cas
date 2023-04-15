(ns cas.comps
  (:require [rum.core :as rum]
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
    [:div (str "  Current value: " (-> (rum/react state/toogleoo) :idx views :name))]
    [:div (str "Mode: " (-> (rum/react state/mode)))]
    [:div (str "Highlight-atom: " (-> (rum/react cas.state/highlight-atom)))]
    [:div (str "tree-atom: " (-> (rum/react cas.state/tree-atom)))]
    [:div (str "keylang-input: " (-> (rum/react cas.state/keylang-input)))]
    [:hr]]
[:div]
   ((->  (rum/react state/toogleoo) :idx views :component))

   #_((views (:idx (rum/react state/toogleoo))))])
