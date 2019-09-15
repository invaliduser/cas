(ns cas.utils
  (:require [cas.shorthand]))

(def alert js/alert)
(cas.shorthand/pull-in math
                       [evaluate
                        compile
                        parse
                        simplify])





