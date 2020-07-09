(ns cas.state
  (:require [cas.test-data]
            [cas.lang-to-tex :refer [compile-to-tex]]
            [cas.nat :as nat]
            [cas.utils :refer [key-gen]]))

(def mode (atom :edit)) ;:edit and :tree for now

(def all-real-path (atom false))

(def tree-atom (atom cas.test-data/default-data))
(def tex (atom ""))
(reset! tex (compile-to-tex (first @tree-atom)))

(def was-write-mode-before? (atom false))

(def highlight-atom (atom [0]))
(def show-paths? (atom false))

(def highlight-atom-2 (atom nil))

(def keystream (atom '[]))
(def keystream-tokenized (atom []))
(def keystream-resolved-tokens)

(def keystream-results (atom '[]))
(def keystream-undecided (atom '[]))


(def last-key (atom nil))
(def keylang-input (atom ""))
#_(add-watch keylang-input :tokenize (fn [k r o n] (reset! keystream-tokenized (tokenize n))))


(defn fall-back-to-memo [f]
  (let [holder (atom "")]
    (fn [& args]
      (println "running a calc fn!")
      (let [res (try (reset! holder (apply f args))
                     (catch js/Error e @holder))]
        res))))


(defn over
  ([f a k]
(let [r-atom (atom nil)]
     (add-watch a k (fn [k r o n]
                      (reset! r-atom (f @a))))
     r-atom))
  ([f a]
   (over f a (key-gen))))

(def roadmap (map #(-> % (assoc :k (key-gen))
                       #_(update % :f fall-back-to-memo))
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
