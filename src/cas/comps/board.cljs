(ns cas.comps.board
  (:require [rum.core :as rum]
            [cljs.spec.alpha :as spec]
            [clojure.walk :as walk]
            [cas.comps.microsoft-directory-tree :as easy-tree]
            [cas.tex-render :refer [render-tex]]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.state :refer [mode tree-atom tex highlight-atom show-paths?]]
            [cas.keys :refer [key-stream-display]]
            [cas.nat :refer [combos-display] :as nat]
            [react]))

(add-watch tree-atom :to-tex (fn [k r o n]
                               (reset! tex (try (compile-to-tex (first n))
                                                (catch :default e (str e))
                                                )))) ;this might be a case for reframe...


(rum/defc full-tex-display < rum/reactive [tex-atom]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (render-tex (rum/react tex-atom)))}}])

(rum/defc mode-indicator < rum/reactive []
  [:span (str (rum/react mode) "-mode")])

(let [c (atom 0)]
  (defn key-gen []
    (swap! c inc)
    (-> @c str keyword)))

(defn over [f a]
  (let [r-atom (atom nil)]
    (add-watch a (key-gen) (fn [k r o n]
                             (reset! r-atom (f @a))))
    r-atom))

(def roadmap [{:name "tokenize-material"}

              {:name "tokenized"
               :f nat/tokenize}

              {:name "parsed"
               :f nat/iparse}

              {:name "compiled-to-tex"
               :f compile-to-tex}])

(defn connect-roadmap! [rm]
  (first (reduce (fn [[m last-key] v]
                   [(assoc m (:name v) (if last-key (over (:f v) (m last-key))
                                           (atom nil)))
                     (:name v)])
                 [{} nil] rm)))

(defonce atom-map (connect-roadmap! roadmap))

(rum/defc render-roadmap < rum/reactive [rm]
  [:div (for [{ename :name :as entry} roadmap]
          [:div
           [:div [:label ename]]
           [:div [:pre [:code (str (rum/react (atom-map ename)))]]]])])


(defn shove-pipe [s]
  (reset! (atom-map "tokenize-material") s))


(rum/defc backdrop < rum/reactive []
  [:div
   [:span {:on-click #(swap! show-paths? not)}  (str
                                                 "path"
                                                 (if (rum/react show-paths?) "(T)" "(F)") ":"
                                                 (rum/react highlight-atom))]
   (mode-indicator)
   [:hr]

   (render-roadmap roadmap)

   #_#_#_#_   (easy-tree/atwrap tree-atom)
   (full-tex-display tex)
   (combos-display)
   (key-stream-display)
])


