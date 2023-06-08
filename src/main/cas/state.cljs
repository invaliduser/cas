(ns cas.state
  (:require [cas.lang-to.tex :refer [compile-to-tex]]
            [cas.data :refer [over atom-> -notify INotifiable]]
            [cas.nat :as nat]
            [cas.utils :refer [key-gen]]
            [datascript.core :as ds]
            [cas.test-data]
            [cas.tree-ops :refer [vget-in remove-last]]
            [rum.core :as rum]))

(defonce db (ds/create-conn))
(defn all-of [eid] (ds/pull @db '[*] eid))

(defn find-old-and-notify-watches [this]
  (let [old (.-last_val ^js this)
        new @this]
    (set! (.-last_val ^js this) new)
    (if (not= old new)
      (-notify-watches this old new))))

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

  INotifiable
  (-notify [this v]
    (find-old-and-notify-watches this))
  
  IWatchable 
  (-notify-watches [this old new]
    (doseq [[k v] (.-watches this)]
        (v k this old new)))
  (-add-watch [this key f]
    #_(do
        (set! (.-watches this) (assoc (.-watches this) key f))
        (let [k (keyword (str id "-" attr ))]
          (ds/listen! db k (fn [{:keys [tx-data db-before db-after] :as tx-report}]
                             (when (some #(= (list id attr) (take 2 %)) tx-data)
                               (let [old (attr (ds/entity db-before id))
                                     new (attr (ds/entity db-after id))]
                                 (when (not= old new)
                                   (-notify this new))))))))

    (ds/listen! db key (fn [{:keys [tx-data db-before db-after] :as tx-report}]
                         (when (some #(= (list id attr) (take 2 %)) tx-data)
                           (let [old (attr (ds/entity db-before id))
                                 new (attr (ds/entity db-after id))]
                             (when (not= old new)
                               (f key this old new)))))))
  (-remove-watch [this key]
    #_(set! (.-watches this) (dissoc (.-watches this) key))
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

(deftype ArbitraryCursor ;no cache
    [read write]
    IAtom
    IDeref
    (-deref [this]
      (read))

    IReset
    (-reset! [this new-value]
      (write new-value))

    ISwap
    (-swap! [a f] (-reset! a (f (-deref a))))
    (-swap! [a f x] (-reset! a (f (-deref a) x)))
    (-swap! [a f x y] (-reset! a (f (-deref a) x y)))
    (-swap! [a f x y more] (-reset! a (apply f (-deref a) x y more)))

    INotifiable
    (-notify [this v]
      (find-old-and-notify-watches this))
    
    IWatchable
    (-add-watch [this key f]
      (set! (.-watches this) (assoc (.-watches this) key f)))
    (-notify-watches [this old new]
      (doseq [[k v] (.-watches this)]
        (v k this old new)))
    (-remove-watch [this key]
      (set! (.-watches this) (dissoc (.-watches this) key))))

(defn arb-cursor
  ([read write set-listen!]
   (let [a (ArbitraryCursor. read write)]
     (set-listen! a)
     a))
  ([read write set-listen! initial-value]
   (let [a (arb-cursor read write set-listen!)]
     (reset! a initial-value)
     a)))

(deftype ReadOnlyCursor [read]
  IAtom
  IDeref
  (-deref [this]
          (read))
  INotifiable
  (-notify [this new]
    (find-old-and-notify-watches this))
  
  IWatchable
  (-add-watch [this key f]
              (set! (.-watches this) (assoc (.-watches this) key f)))
  (-notify-watches [this old new]
                   (doseq [[k v] (.-watches this)]
                     (v k this old new)))
  (-remove-watch [this key]
                 (set! (.-watches this) (dissoc (.-watches this) key))))

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
(def default-highlight [0])
(def problems (db-cursor 1 :problems cas.test-data/test-problems))
(def highlights  (db-cursor 1 :highlights (vec (repeat (count cas.test-data/test-problems) default-highlight))))
(def selected-problem  (db-cursor 1 :selected-problem 0))

(defn atoms-listener [as]
  (fn [target-ref]
    (doseq [atm as]
      (add-watch atm (key-gen) (fn [k r o n]
                                 (-notify target-ref n))))))

(defonce tree-atom (let [read (fn [] (@problems @selected-problem))
                         write  (fn [nv] (swap! problems assoc-in [@selected-problem :tree] nv))
                         set-listen (atoms-listener [problems selected-problem])]
                     (arb-cursor read write set-listen)))

(defonce highlight-atom (let [read (fn [] (@highlights @selected-problem))
                                write (fn [nv] (swap! highlights assoc @selected-problem nv))
                                set-listen (atoms-listener [highlights selected-problem])]
                          (arb-cursor read write set-listen)))

(defonce parent-value (over (fn [tree highlight] (get-in tree (drop-last highlight)))
                            [tree-atom highlight-atom]))



(def db-hold (atom nil))
(defonce curr-value  (let [read (fn [] (vget-in @tree-atom @highlight-atom))
                           write  (fn [nv] (swap! tree-atom assoc-in highlight-atom nv))
                           set-listen (atoms-listener [tree-atom highlight-atom])]
                       (arb-cursor read write set-listen)))

#_(defonce curr-value (over vget-in [tree-atom highlight-atom]))
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



 ; this really does need to move to datascript
