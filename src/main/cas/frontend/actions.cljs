(ns cas.frontend.actions
  (:require
   [cas.frontend.chans :refer [key-chan action-interpreter]]
   [cas.frontend.comps.path-tree :as mdt]
   [cas.frontend.comps.problem-drawer :as pd]
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
                      key-chan ;need to do core.async magic  so these actions can be triggered by non-key sources
                      :after #(println (str "got " % ", path is now " @highlight-atom))))
