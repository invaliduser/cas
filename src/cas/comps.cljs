(ns cas.comps
  (:require [rum.core :as rum]
            [cas.utils :refer [alert parse]]
            [cas.lang-to-tex]
            [cas.comps.board :as board]
            [cljs.tools.reader.edn]
            [cas.tex-render]
            ))


'(#_[cljsjs.react]
    #_react-mathjax)




(def inp (atom "2+2"))
(def parsed (atom "2+2"))
(def texified (atom ""))

#_(add-watch inp :parse-and-update (fn [k r o n]  ;TODO what parse was I referring to?make work
                                   (reset! parsed (parse n))))

(add-watch parsed :texify-and-update (fn [k r o n]
                                       (let [tex (.toTex n)]
                                         (js/console.log tex)
                                         (reset! texified tex))))



(rum/defc editable-textarea < rum/reactive [atm]
  [:div
   [:textarea {:value (rum/react atm)
               :on-change (fn [e] (reset! atm (.. e -target -value)))}]])

(rum/defc rendered-mathjax < rum/reactive [atm]
  [:div {:dangerouslySetInnerHTML {:__html (.-outerHTML (cas.tex-render/render-tex (rum/react atm)))}}])


(rum/defc loader [atm]
  [:div "load stuff here"
   [:input {:on-change (fn [e] (reset! atm (.. e -target -value)))}]])


(def left-side (atom ""))
(def right-side (atom ""))





(def eq-parts-atm (atom {:left "" :right ""})) ;both take tex
(def eq-atom (atom ""))

(defn mk-latex-equation [m]
  (str (:left m) "=" (:right m)))


#_(doseq [[nom ke] [[left-side :left]
                  [right-side :right]]]
 (add-watch nom :texify-and-update (fn [k r o n]
                                     (let [tex (-> n parse (.toTex))]
                                       (swap! eq-parts-atm assoc-in [ke] tex)))))
(add-watch eq-parts-atm :fix (fn [k r o n] (reset! eq-atom (mk-latex-equation n))))


(rum/defc equation-loader []
  [:div
   (loader left-side)
   "="
   (loader right-side)])

(rum/defc eq-watch < rum/reactive []
  [:div "generated latex:" (rum/react eq-atom)])


(rum/defc tex-display < rum/reactive []
  [:div (rum/react texified)])

(def eq-rendered (rendered-mathjax eq-atom))


(def hw-atom (atom ""))

(def hw-disp (editable-textarea hw-atom))
#_(rum/defc hw-disp < rum/reactive []
  [:div
   [:textarea {:value (rum/react hw-atom)
               :on-change (fn [e] (reset! hw-atom (.. e -target -value)))}]])


(defn add-to-hw [new-stuff]
  (swap! hw-atom #(str % new-stuff)))

(rum/defc record-current [atm text]
  [:button {:on-click #(add-to-hw @atm)} text])

(rum/defc rendered-hw < rum/reactive []
;prolly need to get legit mathjax or latex.js to make this an actually good author env, but...
  [:div {:dangerouslySetInnerHTML {:__html
                                    (-> (rum/react hw-atom)
                                        cas.tex-render/render-tex
                                        (.-outerHTML))}}])



(def manipulang-atm (atom ""))
(rum/defc manipulang-edit < rum/reactive []
  [:div
   [:textarea {:value (rum/react manipulang-atm)
               :on-change (fn [e] (reset! manipulang-atm (.. e -target -value)))}]])

(def manipulang-tex-atm (atom ""))
(add-watch manipulang-atm :fix (fn [k r o n] (try
                                               (let [parsed (cljs.tools.reader.edn/read-string n)];just makes data structures, shouldn't feel squeamish 'bout giving this its own atom
                                                 ;we're using the edn reader, but we most likely want to switch to a parser that doesn't freak out at 5x or similar
                                                 
                                                 (reset! manipulang-tex-atm (cas.lang-to-tex/compile-to-tex parsed)))
                                               (catch js/Error e  "a-ha...I just need $50"))))

(rum/defc manipulang-tex-sausage < rum/reactive []
  [:div
   [:textarea {:value (rum/react manipulang-tex-atm)
               :on-change (fn [e] (reset! manipulang-tex-atm (.. e -target -value)))}]])



(def rendered-manipulang  (rendered-mathjax manipulang-tex-atm))


(def toogle (atom true))

(rum/defc main-comp < rum/reactive []
  [:div {:height "100%"}
   [:div
    [:input {:type "button" :on-click #(swap! toogle not) :value "Toggle between board and bench"}]
    [:span (str "  Current value: " (if (rum/react toogle) "board" "bench" ))]
    [:hr]]
   (if (rum/react toogle)
     (board/backdrop)


     [:div#first
      [:div   [:div "uses three atoms: inp, parsed, texified"]
       (loader inp)
       [:div "tex:" (tex-display)]
       (rendered-mathjax texified)
       [:hr]]

      [:div
       (record-current texified "add-to-hw")
       (equation-loader)
       (record-current eq-atom "add-to-hw")
       (eq-watch)
       eq-rendered
       [:div "unrendered contents of hw-atom:"]
       hw-disp
       (rendered-hw)
       [:hr]]

      [:div
       [:div "manipulang-edit"]
       (manipulang-edit)

       [:div "manipulang-tex"]
       (manipulang-tex-sausage)

       rendered-manipulang]])])
