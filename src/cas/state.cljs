(ns cas.state
  (:require [cas.test-data]
            [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.nat :as nat]
            [cas.utils :refer [key-gen]]
            [datascript.core :as ds]))

#_(defn over ; takes a fn and an atom, returns a new atom guaranteed to be (f @a).  feels redundant?
  ([f a k]
   (let [r-atom (atom (f @a))]
     (add-watch a k (fn [k r o n]
                      (reset! r-atom (f n))))
     r-atom))
  ([f a]
   (over f a (key-gen))))

; cache/memoize
; compare timestamps
; this really ought to be done w/channels maybe


(defn over
  ([f as]
     (over f as (key-gen)))
  ([f as k]
   (cond (vector? as)
         (let [arg-cache (atom  (vec (map deref as)))
               r-atom (atom (apply f @arg-cache))]
           (add-watch arg-cache k (fn [k r o n] (reset! r-atom (apply f n))))
           (doseq [i (range (count as))]
             (add-watch (nth as i) (key-gen) (fn [k r o n]
                                               (swap! arg-cache assoc i n))))
           r-atom)
         :else
         (let [r-atom (atom (f @as))]
           (add-watch as k (fn [k r o n]
                             (reset! r-atom (f n))))
           r-atom))))

(deftype Cursor [atm path watches]
  IAtom

  IDeref
  (-deref [this]
    (get-in @atm path))
  IReset
  (-reset! [this new-value]
    (swap! atm assoc-in path new-value))
  ISwap
  (-swap! [a f] (-reset! a (f (-deref a))))
  (-swap! [a f x] (-reset! a (f (-deref a) x)))
  (-swap! [a f x y] (-reset! a (f (-deref a) x y)))
  (-swap! [a f x y more] (-reset! a (apply f (-deref a) x y more)))

  IWatchable
  (-notify-watches [this old new]
    (doseq [[k f] (.-watches this)]
      (f k this old new)))
  (-add-watch [this key f] (let [w (.-watches this)]
                             (set! (.-watches this) (assoc w key f))))
  (-remove-watch [this key] (set! (.-watches this) (dissoc (.-watches this) key)))
  )

(defn cursor [atm path]
  (->Cursor atm path nil))


(defonce db (ds/create-conn))

(defn atom-from-datascript []

  )

(defn switch-atom [limit]
  (atom {:idx 0 :limit limit}))

(def toogleoo (switch-atom 2))
(defn advance! [] ;index vector
  (swap! toogleoo (fn [{:keys [idx limit]}]
                    {:idx (if (< idx limit)
                            (inc idx)
                            0)
                     :limit limit})))

(defonce mode (atom :edit)) ;:edit and :tree for now

(defonce all-real-path (atom true))

(defonce tree-atom (atom cas.test-data/default-data))
(defonce highlight-atom (atom [0]))

(defonce tex (atom ""))
(reset! tex (compile-to-tex (first @tree-atom)))

(defonce write-buffer (atom nil))


(defonce show-paths? (atom false))

(defonce highlight-atom-2 (atom nil))

(def keystream (atom '[]))
(def keystream-tokenized (atom []))
(def keystream-resolved-tokens)

(def keystream-results (atom '[]))
(def keystream-undecided (atom '[]))

(def keylang-input (atom ""))
(def last-key (over last keylang-input))
#_(add-watch keylang-input :tokenize (fn [k r o n] (reset! keystream-tokenized (tokenize n))))


(defn fall-back-to-memo [f]
  (let [holder (atom "")]
    (fn [& args]
      (println "running a calc fn!  memo'd value is:" @holder)
      (let [res (try (reset! holder (apply f args))
                     (catch js/Error e @holder))]
        res))))




(def roadmap (map #(-> % (assoc :k (key-gen))
                       (update :f fall-back-to-memo))
                  [{:name "tokenize-material"}

                   {:name "tokenized"
                    :f nat/tokenize}

                   {:name "parsed"
                    :f nat/iparse}

                   {:name "compiled-to-tex"
                    :f compile-to-tex}]))

(defn connect-roadmap! [rm]
  (first (reduce (fn [[m last-key] v]
                   [(assoc m (:name v) (if last-key (over (:f v) (m last-key))
                                           (atom nil)))
                     (:name v)])
                 [{} nil] rm)))

(defonce atom-map (connect-roadmap! roadmap))

(comment (def safe-div (fall-back-to-memo /))
         (safe-div 3 5)
         (safe-div 4 0))
