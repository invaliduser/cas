(ns cas.state
  (:require [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.data :as data :refer [over]]
            [cas.nat :as nat]
            [cas.utils :refer [key-gen]]
            [datascript.core :as ds]
            [cas.test-data]
            [cas.tree-ops :refer [vget-in remove-last]]
            [rum.core :as rum]
            [cas.data :refer [over atom->]]))

(defonce db (ds/create-conn))
(defn all-of [eid] (ds/pull @db '[*] eid))

(defn cursor-in
  ([ref path]
   (rum/cursor-in ref path))
  ([ref path va]
   (let [res (cursor-in ref path)]
     (reset! res va)
     res)))


(deftype Crystal [q db]
  IDeref
  (-deref [this]
    (ds/q q @db)))

(defn crystal [q] (Crystal. q db))
(deftype DBCursor [id attr db] ;basically takes place of atom but we have datascript powers
  IAtom
  IDeref
  (-deref [this]
    (-> @db (ds/entity id)
        attr))

  IReset
  (-reset! [this new-value]
    (ds/transact! db [[:db/add id attr new-value]])
    new-value)

  ISwap
  (-swap! [a f] (-reset! a (f (-deref a))))
  (-swap! [a f x] (-reset! a (f (-deref a) x)))
  (-swap! [a f x y] (-reset! a (f (-deref a) x y)))
  (-swap! [a f x y more] (-reset! a (apply f (-deref a) x y more)))

  IWatchable 
  (-notify-watches [this old new])
  (-add-watch [this key f]
    (ds/listen! db key (fn [{:keys [tx-data db-before db-after] :as tx-report}]
                         (when (some #(= (list id attr) (take 2 %)) tx-data)
                           (f key this
                              (attr (ds/entity db-before id))
                              (attr (ds/entity db-after id)))))))
  (-remove-watch [this key]
    (ds/unlisten! db key)))

(defn db-cursor
  ([id attr] (DBCursor. id attr db))
  ([id attr va]
   (let [res (DBCursor. id attr db)]
     (reset! res va)
     res)))

(comment "datascript reminders"
         {:schema "https://github.com/kristianmandrup/datascript-tutorial/blob/master/create_schema.md"
          
          }
         (ds/transact! db [[:db/add 25 :fname "daniel"]
                           [:db/add 25 :lname "bell"]])
         ;or
         (ds/transact! db [{:db/id 25
                            :fname "daniel"
                            :lname "bell"}])
         
         (ds/q '[:find ?name
                 :where [25 :fname ?name]] @db)
         (:fname (ds/entity @db 25)))


;--------------------------------------------end infra



(defn switch-atom [limit]
  (atom {:idx 0 :limit limit}))

(def toogleoo (switch-atom 1))
(defn advance! [] ;index vector
  (swap! toogleoo (fn [{:keys [idx limit]}]
                    {:idx (if (< idx limit)
                            (inc idx)
                            0)
                     :limit limit})))


;;;;
(defonce settings (db-cursor 1 :value {}))
(defonce mode (cursor-in settings [:mode] :edit))  ;:edit and :tree for now
(defonce show-paths? (cursor-in settings [:show-paths?] false))

;;;;
(defonce tree-atom (db-cursor 1 :current-tree cas.test-data/default-data))
(defonce highlight-atom (db-cursor 1 :highlight-path [0]))

(defonce parent-value (over #(get-in % (drop-last %2)) [tree-atom highlight-atom]))
(defonce curr-value (over vget-in [tree-atom highlight-atom]))
(defonce parent-path (over remove-last [highlight-atom]))
(def tex (over #(compile-to-tex (first %)) tree-atom ))


;;;;
(def keystream (db-cursor 1 :keystream '[])#_(atom '[]))

(def keystream-resolved-tokens)
(def keystream-results (atom '[]))
(def keystream-undecided (atom '[]))

;;;;
(defonce keylang-input (atom ""))
(defonce write-buffer (atom nil))

(defonce untokenized keylang-input)
(atom-> untokenized
        tokenized nat/tokenize
        parsed nat/iparse
        raw-tex compile-to-tex)

(defonce atom-map {:untokenized untokenized
               :tokenized tokenized
               :parsed parsed
               :raw-tex raw-tex})

(defonce last-key (over last keylang-input))

;;;;
(defn fall-back-to-memo [f]
  (let [holder (atom "")]
    (fn [& args]
      #_(println "running a calc fn!  memo'd value is:" @holder)
      (let [res (try (reset! holder (apply f args))
                     (catch js/Error e @holder))]
        res))))

(comment (def safe-div (fall-back-to-memo /))
         (safe-div 3 5)
         (safe-div 4 0))

(def problems (atom cas.test-data/test-problems))

(def selected-problem (atom 0)) ; this really does need to move to datascript
