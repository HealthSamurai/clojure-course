(ns course-tests.operations
  (:require [db.query :as db]))


(defn default-test-body [{:keys [module chapter file-name test-name full-path]}]
  {:module module
   :chapter chapter
   :file-name file-name
   :test-name test-name
   :full-path full-path
   :passed 0
   :failed 0
   :error 0})


(defn select-test [ctx test-path]
  (db.query/query-one ctx {:ql/type :pg/select
                           :select :*
                           :from :cljtest
                           :where [:= [:jsonb/#>> :body [:full-path]] [:pg/param test-path]]}))


(defn update-test [ctx uuid body]
  (db.query/query ctx {:ql/type :pg/update
                       :update :cljtest
                       :set {:body ^:jsonb/obj [:pg/param body]
                             :updated_at (str (java.time.LocalDateTime/now))}
                       :where [:= :uuid [:pg/param uuid]]}))


(defn create-test [ctx body]
  (let [date-now (str  (java.time.LocalDateTime/now))]
    (db.query/query ctx {:ql/type :pg/insert
                         :into :cljtest
                         :value {:uuid (str (java.util.UUID/randomUUID))
                                 :created_at date-now
                                 :updated_at date-now
                                 :body ^:jsonb/obj [:pg/param body]}})))


(defn update-test-status [test status]
  (cond-> test
    (get test status) (update status inc)
    :always           (assoc :status status)))



(defn update-test-list-status [ctx test-list]
  (let [db-test-list (db.query/query ctx {:ql/type :pg/select
                                          :select :*
                                          :from :cljtest})
        db-test-set (set (map (comp :full-path :body) db-test-list))
        test-set (set (map :full-path test-list))
        tests-to-remove (clojure.set/difference
                          db-test-set
                          #{nil}
                          test-set)
        new-tests (clojure.set/difference
                   test-set
                   db-test-set)

        tests-map (if (> (count new-tests) 0)
                    (apply array-map
                           (mapcat #(vector (:full-path %) %)
                                   test-list))
                    {})
        tests-to-create (vals (reduce
                               #(assoc %1 %2 (get tests-map %2))
                               {}
                               new-tests))]
    (when (seq tests-to-remove)
      (db.query/delete-tests ctx tests-to-remove))

    (when (seq tests-to-create)
      (doseq [test tests-to-create]
        (create-test ctx (assoc (default-test-body test) :status :new))))))
