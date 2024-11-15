(ns cas.frontend.comps.pdf
  (:require  
   [cas.frontend.chans :refer [pdf-chan]]
   [rum.core :as rum :refer-macros [defc]]
   [cljs.core.async :refer [go go-loop <!]]
   [cljs.core.async.interop :refer-macros [<p!]]))


;;trying two methods of pdf rendering:pdf.js, immediately below, and latex.js, below that
;;another possibility is texlive, an emscriptem-ized version of pdflatex (so it runs in browser!) https://manuels.github.io/texlive.js/#open_pdf

;;the pdf.js version ---https://mozilla.github.io/pdf.js/examples/

                                        ;possibly relevant: https://medium.com/@pdx.lucasm/canvas-with-react-js-32e133c05258


(defc pdf-canvas []
  (let [ref (rum/use-ref nil)]
    (rum/use-effect! #(.log js/console (rum/deref ref)))
    [:div
     "hi --- run (render-pdf \"ok.pdf\") to see me"
     [:canvas {:id "canvas" :ref ref} ]]))

(defn get-pdf-promise [task]
  (.-promise ^js task))

(defn get-pdf-page-promise [pdf]
  (.getPage ^js pdf 1))

(defn get-page-viewport [page m]
  (.getViewport ^js page m))


(defn render-page! [page]
  (let [scale 1.5
        viewport (get-page-viewport page #js {:scale scale})
        output-scale (or (.-devicePixelRatio js/window) 1)
        canvas (.getElementById js/document "canvas")
        _ (set! (.-width canvas)  (.floor js/Math (* output-scale (.-width viewport))))
        _ (set! (.-height canvas) (.floor js/Math (* output-scale (.-height viewport))))
        #_#_            _ (set! (.. canvas -style -width)  (.floor js/Math (str (.-width viewport) "px")))
        #_#_            _ (set! (.. canvas -style -height) (.floor js/Math (str (.-height viewport) "px")))

        _ (set! (.-width (.-style canvas))  (.floor js/Math (str (.-width viewport) "px")))
        _ (set! (.-height (.-style canvas)) (.floor js/Math (str (.-height viewport) "px")))

        context (.getContext canvas "2d")

        transform (if (not= output-scale 1)
                    #js [output-scale 0 0 output-scale 0 0]
                    nil)

        render-context #js {:canvasContext context
                            :transform transform
                            :viewport viewport}]
    (.render page render-context)))



(defn render-pdf [src] ;url, or array
  (let [loading-task (.getDocument js/pdfjsLib src)]
    (go
      (let [pdf (<p! (get-pdf-promise loading-task))
            page  (<p! (get-pdf-page-promise pdf))]
        (render-page! page)))))


(defn start-render-loop! []
  (println "starting pdf-renderloop!")
  (go-loop []
    (render-pdf (<! pdf-chan))))


;;the latex.js version

; see here https://latex.js.org/usage.html#in-the-browser ,

; here , https://stackoverflow.com/questions/58200373/how-to-append-child-to-react-element

; and here https://legacy.reactjs.org/

; latex.js returns a dom node
; strategy: get a ref to underlying node of react component, append to that

#_(ns bla (:require
         ["latex.js" :as ljs]))
#_(defc latex-html5 []
    [:div "ht5"])
;shadow-cljs doesn't like the dynamic import in latex.js.  Might be possible to pack in ljs's deps beforehand with webpack:  https://code.thheller.com/blog/shadow-cljs/2020/05/08/how-about-webpack-now.html#option-2-js-provider-external
