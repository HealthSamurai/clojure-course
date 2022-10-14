(ns web.operations
  (:require [web.unifn :as u]
            [web.rpc]
            [db.query]
            [cheshire.core]
            [hiccup.core]
            [hiccup.page]))


(defmethod u/*fn ::rpc [{:as ctx,
                         {op :resource} :request}]
  (let [res (web.rpc/rpc-call ctx op)]
    {:response
     (if (:error res)
       {:status 422 :body res}
       {:status 200 :body res})}))



(defmethod u/*fn ::root [ctx]
  {:response {:status 200
              :body (hiccup.page/html5
                      [:body
                       [:div#root]
                       [:script {:src (str "/static/js/frontend.js")}]])}})


(defmethod web.rpc/rpc 'rpc-ops/test-rpc
  [ctx req]
  {:result "TEST RPC FIRED :)"})


(defmethod u/*fn ::test-db [ctx]
  (db.query/query ctx {:ql/type :pg/create-table
                       :table-name :mytable
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

(defn select-test [ctx test-path]
  (db.query/query-one ctx {:ql/type :pg/select
                           :select :*
                           :from :cljtest
                           :where [:= [:jsonb/#>> :body [:full-path]] [:pg/param test-path]]}))

(defn update-test [ctx uuid body]
  (db.query/query ctx {:ql/type :pg/update
                       :update :cljtest
                       :set {:body ^:jsonb/obj [:pg/param body]}
                       :where [:= :uuid [:pg/param uuid]]}))

(defn create-test [ctx body]
  (db.query/query ctx {:ql/type :pg/insert
                       :into :cljtest
                       :value {:uuid (str (java.util.UUID/randomUUID))
                                :body ^:jsonb/obj [:pg/param body]}}))

(defn update-test-status [test status]
  (-> test
      (update status inc)
      (assoc :status status)))

(defn default-test-body [ns test-name]
  {:ns ns
   :test-name test-name
   :full-path (str ns \/ test-name)
   :passed 0
   :failed 0
   :error 0})

(defmethod web.rpc/rpc 'rpc-ops/toggle-test
  [ctx {{:keys [ns test-name status]} :params :as req}]
  (try
    (let [full-path (str ns \/ test-name)]
      (if-let [{body :body uuid :uuid} (select-test ctx full-path)]
        (update-test ctx uuid (update-test-status body status))
        (create-test ctx (update-test-status (default-test-body ns test-name) status))))
    {:result "OK"}
    (catch Exception e
      (println (ex-message e))
      {:error (ex-message e)})))
