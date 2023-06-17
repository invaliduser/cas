(ns cas.frontend.actions
  (:require
   [cas.frontend.chans :refer [key-chan action-interpreter]]
   [cas.comps.microsoft-directory-tree :as mdt]
   [cas.comps.problem-drawer :as pd]
   [cas.frontend.state :refer [highlight-atom]]))

(defn init-actions! []
  (action-interpreter "tree-manip" {:left mdt/left!
                                    :right mdt/right!
                                    :down mdt/down!
                                    :up mdt/up!
                                    :select-operator mdt/select-operator!
                                    :toggle-parens mdt/toggle-parens!
                                    :select-top mdt/select-top!
                                    :delete mdt/delete!
                                    :extend-left mdt/extend-left!
                                    :extend-right mdt/extend-right!

                                    :problem-up pd/problem-up!
                                    :problem-down pd/problem-down!

                                    #_:compile-to-pdf
                                    }
                      key-chan
                      :after #(println (str "got " % ", path is now " @highlight-atom))))
