(ns cas.comps.pdf
  (:require [rum.core :as rum]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

;;trying two methods of pdf rendering:pdf.js, immediately below, and latex.js, below that
;;

;;the pdf.js version ---https://mozilla.github.io/pdf.js/examples/

;possibly relevant: https://medium.com/@pdx.lucasm/canvas-with-react-js-32e133c05258
(rum/defc pdf-canvas []
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

(defn render-pdf [url] ;this will obv not scale if it's routebased lel
  (let [loading-task (.getDocument  js/pdfjsLib "ok.pdf")]
    (go
      (let [pdf (<p! (get-pdf-promise loading-task))
            page  (<p! (get-pdf-page-promise pdf))
            scale 1.5
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
        (.render page render-context)))))

;;the latex.js version

; see here https://latex.js.org/usage.html#in-the-browser ,

; here , https://stackoverflow.com/questions/58200373/how-to-append-child-to-react-element

; and here https://legacy.reactjs.org/
