(ns cas.comps
  (:require [rum.core :as rum]
            [cas.lang-to.tex]
            [cljs.tools.reader.edn]
            [cas.tex-render]
            [cas.comps.board :as board]
            [cas.views.bench :as bench]
            [cas.views.tree-manip :as tree-manip]
            [cas.state :as state]
            [cas.shorthand :as sh]))

(def atm (atom nil))
(def mfnode (atom nil))
(rum/defcs mfield < (rum/local  "\\frac{3}{4}" :default)
  [state]
  (let [v (:default state)]
    [:div
     @v
     [:math-field {:ref (fn [node] (reset! mfnode node))
                   :read-only true
                   :value @v
                   #_#_:on-input #(do (println (.. % -target -value))
                                  (js/console.log (.. % -target -expression -json)))
                   #_#_:onChange #(reset! atm (.. % -target -value  ))}]
     [:button {:on-click #(.extendSelectionDownwards @mfnode) } "click"]]))

(rum/defc mathml-try []
  [:math
   [:mroot
    [:mn "5"]
    [:mn "3"]]])

(def views [{:component tree-manip/tree-manip-harness :name "tree manipulation harness"}
            {:component mathml-try :name "mathml"}
            {:component mfield :name "mathlive test"}
            {:component board/backdrop :name "backdrop"}
            {:component bench/bench-comp :name "bench"}])

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
