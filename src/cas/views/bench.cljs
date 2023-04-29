(ns cas.views.bench
  (:require [rum.core :as rum]
            [cas.comps.basics :as basics]
            [cas.utils :as utils]))


(def hw-atom (atom ""))


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
                                                 
                                                 (reset! manipulang-tex-atm (cas.lang-to.tex/compile-to-tex parsed)))
                                               (catch js/Error e  "a-ha...I just need $50"))))

(rum/defc manipulang-tex-sausage < rum/reactive []
  [:div
   [:textarea {:value (rum/react manipulang-tex-atm)
               :on-change (fn [e] (reset! manipulang-tex-atm (.. e -target -value)))}]])



(def rendered-manipulang  (basics/full-tex-display manipulang-tex-atm))


(rum/defc editable-textarea < rum/reactive [atm]
  [:textarea {:on-change #(reset! atm (.. % -target -value))
              :value   (rum/react atm)}])


(def hw-disp (editable-textarea hw-atom))

(defn mk-latex-equation [m]
  (str (:left m) "=" (:right m)))

(def eq-atom (atom ""))

(def left-side (atom ""))
(def right-side (atom ""))

(rum/defc eq-watch < rum/reactive []
  [:div "generated latex:" (rum/react eq-atom)])


(defn add-to-hw [new-stuff]
  (swap! hw-atom #(str % new-stuff)))

(rum/defc record-current [atm text]
  [:button {:on-click #(add-to-hw @atm)} text])

(def eq-rendered (basics/full-tex-display eq-atom))

(def inp (atom "2+2"))
(def parsed (atom "2+2"))
(def texified (atom ""))

(add-watch inp :parse-and-update (fn [k r o n]  ;TODO what parse was I referring to?make work
                                   (reset! parsed (utils/parse n))))

(add-watch parsed :texify-and-update (fn [k r o n]
                                       (let [tex (.toTex ^js n)]
                                         (js/console.log tex)
                                         (reset! texified tex))))

(def eq-parts-atm (atom {:left "" :right ""})) ;both take tex

(doseq [[nom ke] [[left-side :left]
                  [right-side :right]]]
(add-watch nom :texify-and-update (fn [k r o n]
                                     (let [tex (-> ^js n
                                                   ^js (utils/parse)
                                                   (.toTex))]
                                       (swap! eq-parts-atm assoc-in [ke] tex)))))
(add-watch eq-parts-atm :fix (fn [k r o n] (reset! eq-atom (mk-latex-equation n))))


(rum/defc loader [atm]
  [:div "load stuff here"
   [:input {:on-change (fn [e] (reset! atm (.. e -target -value)))}]])

(rum/defc equation-loader []
  [:div
   (loader left-side)
   "="
   (loader right-side)])

(rum/defc bench-comp < rum/reactive []
  [:div#first
   [:div [:div "uses three atoms: inp, parsed, texified"]

    [:div "inp:" ]
     (loader inp)
    [:div "parsed:" (rum/react parsed)]
    [:div "by \"parsed\" we are using " [:a {:href "https://github.com/josdejong/mathjs"} "MathJS"] "'s 'parse' method"]
    [:div "tex:" (rum/react texified)]
    (basics/full-tex-display texified)
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

    rendered-manipulang]])
