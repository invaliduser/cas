(ns cas.comps.main
  (:require [rum.core :as rum]
            [cas.lang-to.tex]
            [cljs.tools.reader.edn]
            [cas.tex-render]
            [cas.comps.basics :as basics]
            [cas.comps.board :as board]
            [cas.views.tree-manip :as tree-manip]
            [cas.frontend.state :as state]
            [cas.shorthand :as sh]))

(def views [{:component tree-manip/tree-manip-harness :name "tree manipulation harness"}
            {:component board/backdrop :name "backdrop"}])

(rum/defc main-comp < rum/reactive []
  [:div {:height "100%"}
   [:div
    [:input {:type "button" :on-click state/advance! :value "Advance!"}]
    [:div (str "  Current value: " (-> (rum/react state/toogleoo) :idx views :name))]
    [:div (str "Mode: " (-> (rum/react state/mode)))]
    [:div (str "tree-atom: " (-> (rum/react state/tree-atom)))]
    [:div (str "Highlight-atom: " (-> (rum/react state/highlight-atom)))]
    [:div (str "value at path " (rum/react state/curr-value))]
    [:div (str "keylang-input: " (-> (rum/react state/keylang-input)))]
    [:div "uncompiled tex: "  (rum/react state/tex) ]
    [:div "tex display:  " (basics/full-tex-display state/tex)]

    [:hr]]
[:div]
   ((->  (rum/react state/toogleoo) :idx views :component))

   #_((views (:idx (rum/react state/toogleoo))))])
