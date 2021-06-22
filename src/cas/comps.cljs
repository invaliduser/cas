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



(defn advance! [atm] ;index vector
  (swap! atm (fn [{:keys [idx limit]}]
               {:idx (if (< idx limit)
                       (inc idx)
                       0)
                :limit limit})))

(def views [board/backdrop
            bench/bench-comp
            tree-manip/tree-manip-harness])



(rum/defc main-comp < rum/reactive []
  [:div {:height "100%"}
   [:div
    [:input {:type "button" :on-click #(advance! state/toogleoo) :value "Advance!"}]
    [:span (str "  Current value: " (:idx (rum/react state/toogleoo)))]
    [:hr]]
   ((views (:idx (rum/react state/toogleoo))))])
