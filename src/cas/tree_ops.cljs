(ns cas.tree-ops)

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

(defn replace-last [ve va]
  (let [c (count ve)]
    (assoc ve (dec c) va)))

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

(defn get-currents [leaf]
  (cond (int? leaf) [leaf (inc leaf)]
        (vector? leaf) leaf))

(defn at-left-edge? [curr-left] ;left is inclusive, but we don't want to go left from 1=>0
  (>= 1 curr-left))
(defn at-right-edge? [curr-right v] ;right is exclusive
  (>= curr-right (count v)))

(defn extend-right [p tree]
  (let [leaf (last p)
        [curr-left curr-right] (get-currents leaf)
        parent (get-in tree (drop-last p))]

    (if (at-right-edge? curr-right parent) p                  ; if at edge, do nothing
        (vassoc p -1                ; else
                [curr-left       ;extending right leaves left as is
                 (inc curr-right)]))))

(defn extend-left [p tree]
  (let  [leaf (last p)
         [curr-left curr-right] (get-currents leaf)]
    (if (at-left-edge? curr-left) p                                 ; if at edge, do nothing
        (vassoc p -1                               ; else
                [(dec curr-left)         ;extending right leaves left as is
                 curr-right]))))

(defn up [p tree] (prim-up p))
(defn down [p tree] (prim-down p tree))

(defn left [p tree]
  (let [[curr-left _] (get-currents (last p))]
    (cond (at-left-edge? curr-left)
          (do (println "illegal")
              p)
          :else (replace-last p (dec curr-left)))))
(defn right [p tree]
  (let [[_ excl-right] (get-currents (last p))] ;excl-right is one higher than actual current right, b/c exclusive
    (cond (at-right-edge? excl-right (get-in tree (remove-last p)))
          (do (println "illegal")
              p)
          :else
          (replace-last p excl-right)))) ;seems like doing nothing, 

(defn remove-at-index  [v identifier]
  (cond (int? identifier)
        (into (subvec v 0 identifier)
              (subvec v (inc identifier)))
        (vector? identifier)
        (into (subvec v 0 (first identifier))
              (subvec v  (second identifier)))))

(defn delete-at [tree p]
  (case (count p)
    1
    (remove-at-index tree (last p))
    (update-in tree (nsubvec p 0 -1) remove-at-index (last p)))) ; there's a pattern to extract here

(defn vinsert
  "idx is either an int or a vector of length 1.  If idx is a vector of length 1, splices in."
  [ve idx valu]
  (cond (int? idx)
        (-> (subvec ve 0 idx)
            (conj valu)
            (into (subvec ve idx)))

        (vector? idx)
        (-> (subvec ve 0 (first idx))
            (into valu)
            (into (subvec ve (first idx))))))

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

(defn vget
  ([v identifier]
   (cond (int? identifier)
         (get v identifier)

         (vector? identifier)
         (subvec v (identifier 0) (inc (identifier 1)))))
  ([v identifier not-found]
   (throw "you should probably implement not-found for vget")))

(defn vget-in
  ([m path]
   (reduce vget m path))
  ([m path not-found]
   (throw "you should probably implement not-found for vget-in")))

                                        ;want to iterate through and see if disqualifies
                                        ;when we talk about "ancestor", the in-between relations can be nodal (is a leaf of a branch) or logical (is an arg)

(defn nodal-descendant
  ;[parent-path descendant-path]
  ;the value at (get-in tree d) is contained in (get-in tree p)
  [p d] 
  (let [cp (count p)]
    (and (<= cp (count d))
         (let [lp (last p)
               md (d (dec cp))]    ;value at d at same index as lp
           (and (= (remove-last p) (subvec d 0 (dec cp)))  ;everything but leaf mattches
                (or (and (int? lp) ;leaf matches
                         (= lp md))
                    (and (vector? lp)
                         (>= md (first lp))
                         (< md (last lp)))))))))
;this is correct, but could be made clearer


(defn logical-descendant [p d] ;the value at d is the *logical* descendant of the value at p
  (and (= 0 (last p))
       (nodal-descendant (remove-last p) d)))



#_(defn direct-parent? [p d]
    (and   (<=  (- (count d) (count (take-while-matching p d)))
                1)

           (= (remove-last p) (subvec d 0 (dec (count p))))

           (or
                                        ;node parent
            (and (= (last p) 0)   ;logical parent (within same vector)
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

#_(comment
  (def tdata  ["=" ["+" 2 3 4] ["-" 999  ["+" 988 2]]])

)
