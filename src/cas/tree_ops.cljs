(ns cas.tree-ops
  (:require [cas.state :refer [tree-atom]]))

(defn children [node] (if (vector? node) (next node) nil))

(def children? children)
(defn represents-fn? [p] (= 0 (last p)))

(defn remove-last [v]
  (subvec v 0 (dec (count v))))

(defn doto-last [v f]
  (let [c (count v)]
    (update v (dec c) f)))

(defn node-val [node]
  (if (vector? node)
    (first node)
    node))



(defn fake-path [real-path] ;real path-to-node -> fake-path-to-node
  (apply vector 0 (map dec real-path)))
(defn real-path [our-path]  ; takes our-path and gives path *TO NODE*
  (mapv inc (rest our-path)))

(defn tree-get [t p]
  (get-in t (real-path p)))

(defn reset-node [node v]
  (if (vector? node)
    (apply vector v (rest node))
    v))

(defn update-node [node f & args]
  (if (vector? node)
    (apply vector (apply f node args) (rest node) )
   (apply f node args)))

(defn reset-at-path! [p v]
  (swap! tree-atom update-in (real-path p) reset-node v))

(defn update-at-path! [p f & args]
  (swap! tree-atom update-in (real-path p) #(apply update-node % f args)))

(defn append-at-path! [p v]
  (update-at-path! p (comp js/parseInt str) v))




;          our-path     ;real-path
; ["="     [0]           [0]
;  ["+"    [0 0]         [1 0]          disappeared into the jump
;     2    [0 0 0]       [1 1]          twice
;     3    [0 0 1]       [1 2]          predict diff by jump?
;     4]   [0 0 2]       [1 3] ;col in second incs with col+1 (from left) in left
;  ["-"    [0 1 ]        [2 0]         
;    999   [0 1 0]       [2 1]
;    ["+"  [0 1 1]       [2 2 0]  ;if (last real-path) is 0, is fn
;      988 [0 1 0 0]     [2 2 1]  ;if lrp !=0, isn't fn
;      2]]][0 1 0 1]     [2 2 2]  our-path is 1 longer for non-fns

;;so 0 can be ignored for real-path...and should be


;          our-path     ;path-of-node
; ["="     [0]           []
;  ["+"    [0 0]         [1]   
;     2    [0 0 0]       [1 1] 
;     3    [0 0 1]       [1 2] 
;     4]   [0 0 2]       [1 3] 
;  ["-"    [0 1 ]        [2]         
;    999   [0 1 0]       [2 1]
;    ["+"  [0 1 1]       [2 2] 
;      988 [0 1 1 0]     [2 2 1]
;      2]]][0 1 1 1]     [2 2 2]





#_(comment
  (def tdata  ["=" ["+" 2 3 4] ["-" 999  ["+" 988 2]]])




  (defn big-juju [p]
    (node-val (get-in tdata (real-path p )))) ;takes path-to-node and gives val


  (defn reverse-juju [p]
    (tree-get tdata (fake-path p)))

  (def our-paths
    [[0]
     [0 0]
     [0 0 0]
     [0 0 1]
     [0 0 2]
     [0 1]  
     [0 1 0]
     [0 1 1]
     [0 1 1 0]     
     [0 1 1 1]])

  (def real-paths
    [[]
     [1]   
     [1 1] 
     [1 2] 
     [1 3] 
     [2]         
     [2 1]
     [2 2] 
     [2 2 1]
     [2 2 2]]))
