(ns cas.tree-ops
  (:require [cas.state :refer [tree-atom]]))

(defn children [node] (if (vector? node) (next node) nil))

(def children? children)
(defn represents-fn? [p] (= 0 (last p)))

(defn vassoc [v idx nv]
  (if (> idx -1)
    (assoc v idx nv)
    (assoc v (+ idx (count v)) nv)))

(defn vupdate [v idx f & args]
  (if (> idx -1)
    (apply update v idx f args)
    (apply update v (+ idx (count v)) f args)))


(defn inclusive-neg [v neg-idx?]
  (if (> neg-idx? -1)
    neg-idx?
    (+ neg-idx? (dec (count v)))))

(defn exclusive-neg [v neg-idx?]
  (if (> neg-idx? -1)
    neg-idx?
    (+ neg-idx? (count v))))

(defn nsubvec
  ([v start]
   (subvec v (inclusive-neg v start)))
  ([v start end]
   (subvec v (inclusive-neg v start) (exclusive-neg v end))))

(defn remove-last [v]
  (subvec v 0 (dec (count v))))

(defn doto-last [v f]
  (let [c (count v)]
    (update v (dec c) f)))

(defn node-val [node]
  (if (vector? node)
    (first node)
    node))




(defn fake-path [real-path]    ;real path-to-node -> fake-path-to-node
  (apply vector 0 (map dec real-path)))
(defn real-path [our-path]  ; takes our-path and gives path *TO NODE*
  (mapv inc (rest our-path)))


(defn prim-up [p] (remove-last p))

(defn prim-down [p tree]
  (if (vector? (get-in tree p))
    (conj p 0)
    (vassoc p -1 1)))

(defn prim-right [p] (vupdate p -1 inc))
(defn prim-left [p] (vupdate p -1 dec))

(defn up [p tree] (prim-up p))
(defn down [p tree] (prim-down p tree))

(defn left [p tree]
  (cond (#{0 1} (last p))
        (do (println "illegal")
            p)
        :else (prim-left p)))
(defn right [p tree]
  (cond (= 0 (last p))
        p
        (> (count (get-in tree (remove-last p))) (inc (last p)))
        (prim-right p)
        :else (do (println "illegal")
                  p)))


(defn take-while-matching [a b]
  (let [shortest (min (count a) (count b))]
    (loop [idx 0]
      (cond
        (>= idx shortest)
        (subvec a 0 idx)

        (not= (a idx) (b idx))
        (subvec a 0 idx)
        
        :else
        (recur (inc idx))))))

;want to iterate through and see if disqualifies
;when we talk about "ancestor", the in-between relations can be nodal (is a leaf of a branch) or logical (is an arg)
;


(defn nodal-descendant [p d] ;the value at (get-in tree d) is contained in (get-in tree p)
  (and (<= (count p) (count d))
       (= p (subvec d 0 (count p)))))

(defn logical-descendant [p d] ;the value at d is the *logical* descendant of the value at p
  (and (= 0 (last p))
       (nodal-descendant (remove-last p) d)))



#_(defn direct-parent? [p d]
  (and   (<=  (- (count d) (count (take-while-matching p d)))
              1)

         (= (remove-last p) (subvec d 0 (dec (count p))))

         (or
                                        ;node parent
          (and (= (last p) 0)     ;logical parent (within same vector)
               (not= (last ))
               )    
          )
         )
  )

#_(defn ancestor? [ma md]             ;maybe-ancestor maybe-descendant
                                        ;ma-last ==0, md matches until last, which is !=0
                                        ;ma is contained within md 

    (let [mac (count ma)
          macd (dec mac)
          mdc (count md)
          possible? (<= mac mdc)
          but-last-matches (= (take macd ma)
                              (take macd md))
        
        

        
        
          ]


      (loop [idx 0]
        (if (not= (ma idx) (md idx))
          false)
      
        (if (= idx mal)
          true
          )
      
      
        (cond (= idx mac)
              (or (= mac ()))
              ()
              )

        (if ()

          (and (not= (ma idx) (md idx))
               (< idx mac)
               
               ))
        ))
  

    (= (drop-last ma) (take (dec (count ma)) md)) ; this must be true
                                        ;either 
    (= ma (take (count ma) md))
                                        ;or
    (and    (= (drop-last ma) (take (dec (count ma)) md))
            (= (last ma) 0)
            (not= (last md) 0))


                                        ;check the counts
                                        ;either they're the same and they must be the same except for last
                                        ;or they're different and ma must be contained in last 
  
    (or
     (drop-last ma)
     )
    )

(defn tree-get [t p]
  (let [rp (real-path p)]
    (if (= rp [])
      t
      (get-in t (real-path p)))))

(defn query-get  ;the goal is for this to replace tree-get
                                        ;this will perhaps not be hard, as tree-get is used only in comments apparently
  "takes a path, and returns the item(s) at that location

The last item is special."
  [path tree]
  (let [special (last path)]
    (cond (number? special)
	(get-in tree path)

      :else
      (let [cr (if (< (count path) 2)
                 tree
                (get-in tree (drop-last path)))] ;contextual-root

        (cond (vector? special)
              (subvec cr (first special) (inc (last special)))

              (= :children special) 
              (subvec cr 1)

              (= special :all)
              tree)))))





(defn reset-node [node v]
  v)

(defn update-node [node f & args]
  (apply f node args))

;-----the above two shouldn't exist.  when the below fns call `real-path`, they handle the ambiguity the `vector?` call is necessary for

(defn reset-at-path! [p v]
  (let [rp (real-path p)]
    (if (= rp [])
      (reset! tree-atom [v])
      (swap! tree-atom assoc-in rp v))))

(defn full-reset-at-path! [p v]
  (let [rp (real-path p)]
    (if (= rp [])
      (reset! tree-atom v)
      (swap! tree-atom assoc-in rp v))))

(defn update-at-path! [p f & args]
  (let [rp (real-path p)]
    (if (= rp [])
      (swap! tree-atom  #(apply update-node % f args))
      (swap! tree-atom update-in (real-path p) #(apply update-node % f args)))))

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
