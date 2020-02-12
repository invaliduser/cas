(ns cas.utils
  (:require [cas.shorthand]))

(def alert js/alert)
(cas.shorthand/pull-in js/math
                       [evaluate
                        compile
                        parse
                        simplify])
