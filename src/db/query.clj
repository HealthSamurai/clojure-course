(ns db.query
  (:require [next.jdbc :as jdbc]
            [dsql.pg]
            [dsql.core]
            [next.jdbc.prepare :as prepare]
            [next.jdbc.result-set :as rs]
            [cheshire.core])
  (:import [org.postgresql.util PGobject]
           [java.sql PreparedStatement]))


(def ->json cheshire.core/generate-string)


(defn <-json [d]
  (cheshire.core/parse-string d keyword))


(defn ->pgobject
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))


(defn <-pgobject
  [^org.postgresql.util.PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (when value
        (with-meta (<-json value) {:pgtype type}))
      value)))


(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (->pgobject m)))

  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (->pgobject v))))


(extend-protocol rs/ReadableColumn
  org.postgresql.util.PGobject
  (read-column-by-label [^org.postgresql.util.PGobject v _]
    (<-pgobject v))
  (read-column-by-index [^org.postgresql.util.PGobject v _2 _3]
    (<-pgobject v)))


(defn query [{:as _ctx, :keys [db]} query]
  (jdbc/execute! db (dsql.core/format {} query)
                 {:return-keys true :builder-fn rs/as-unqualified-lower-maps}))

(defn query-one [{:as _ctx, :keys [db]} query]
  (jdbc/execute-one! db (dsql.core/format {} query)
                 {:return-keys true :builder-fn rs/as-unqualified-lower-maps}))

(defn delete-tests [{:as _ctx, :keys [db]} tests]
  (jdbc/with-transaction [tx db]
    (doseq [test tests]
      (jdbc/execute!
       tx
       (dsql.core/format {} {:ql/type :pg/delete
                             :from :cljtest
                             :where [:=
                                     [:jsonb/#>> :body [:full-path]]
                                     [:pg/param test]]})
       {:return-keys true :builder-fn rs/as-unqualified-lower-maps}))))
