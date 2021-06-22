(ns cas.tex-render)


(defn render-tex [tex]
  (.tex2svg js/MathJax tex))


"https://github.com/mathjax/MathJax-src/tree/master/ts/a11y"



"https://github.com/mathjax/MathJax-src/blob/master/ts/a11y/explorer/Region.ts"

"http://docs.mathjax.org/en/latest/options/accessibility.html#semantic-enrich-extension-options"
"with this option on, the html is annotated"


"https://github.com/ianlucas/mathjax-editor/blob/next-v4/src/Editor.js"
"useful for his cursor work, and however he's talking to mathjax"

"https://github.com/krautzource/mathjax-sre-walker/blob/master/docs/source/rewriteEnrichedEquation.js"
"here is an example of a treewalker (kinda interesting); the example does pre-processing based on the semantic enrichment (much more interesting)"



"so...much to think about.  Ideally you could look at the structure TexParser (or whatever) produces, and if you can interface directly with that.  

 - if you understand the semantic enrichment scheme, and their core datastructure, and can connect them to each other and to the raw Tex...

basically the goal is to complete the loop!  Can you, from an element click, get back to the right path in the ast?  Can you decompile the ast into tex?  Can you propagate a change in the ast to the tex, while maintaining relative position?"


                                        ;good papers by the mathjax team: https://mathjax.github.io/papers/MathUI15/mathui15.pdf

; https://ieeexplore.ieee.org/document/7444948
