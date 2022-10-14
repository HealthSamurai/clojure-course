(ns web.operations
  (:require [web.unifn :as u]
            [web.rpc]
            [db.query]
            [cheshire.core]))


(defmethod u/*fn ::rpc [{:as ctx,
                         {op :resource} :request}]
  (let [res (web.rpc/rpc-call ctx op)]
    {:response
     (if (:error res)
       {:status 422 :body res}
       {:status 200 :body res})}))


(defmethod u/*fn ::root [ctx]
  {:response {:status 200
              :body (with-out-str (clojure.pprint/pprint ctx))}})


(defmethod web.rpc/rpc 'rpc-ops/test-rpc
  [ctx req]
  {:result "TEST RPC FIRED :)"})


(defmethod u/*fn ::test-db [ctx]
  (db.query/query ctx {:ql/type :pg/create-table
                       :table-name "mytable"
                       :if-not-exists true
                       :columns {:id          {:type "text" :primary-key true}
                                 :fields     {:type "jsonb"}}})

  (db.query/query ctx {:ql/type :pg/insert
                       :into :mytable
                       :value {:id (str (java.util.UUID/randomUUID))
                               :fields (cheshire.core/generate-string
                                          {:a "test" ;;HACK FIXME
                                           :b "b"})}
                       :returning :*})

  (let [mytable-res (db.query/query ctx {:ql/type :pg/select
                                         :select  :*
                                         :from    :mytable})]
    {:response
     {:status 200
      :body mytable-res}}))
