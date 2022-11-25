(ns web.operations
  (:require [web.unifn :as u]
            [web.rpc]
            [db.query]
            [cheshire.core]
            [hiccup.core]
            [hiccup.page]
            [org.httpkit.server :as server]
            [course-tests.operations :as ctop]
            [clojure.walk]))


(defmethod u/*fn ::rpc [{:as ctx,
                         {:as request, op :body} :request}]
  (let [res (web.rpc/rpc-call ctx op)]
    {:response
     (if (:error res)
       {:status 422 :body res :headers {"Content-Type" "application/json"}}
       {:status 200 :body res :headers {"Content-Type" "application/json"}})}))


(defmethod u/*fn ::root [ctx]
  {:response {:status 200
              :body (hiccup.page/html5
                     [:head
                      [:link {:href "/static/css/stylo.css"
                              :type "text/css"
                              :rel  "stylesheet"}]
                      [:link {:href "/static/css/main.css"
                              :type "text/css"
                              :rel  "stylesheet"}]]
                     [:body
                      [:style "font-family: product"]
                      [:div#root]
                      [:script {:src (str "/static/js/frontend.js")}]])}})


(defmethod u/*fn ::websocket [{:as ctx
                               {:as request} :request}]
  {:response (server/as-channel
              request
              {:on-open (fn [ch]
                          (swap! (:_self ctx) assoc :ws/connection ch))
               :on-close (fn [ch status]
                           (swap! (:_self ctx) dissoc :ws/connection))})})


(defmethod web.rpc/rpc 'rpc-ops/test-rpc
  [ctx req]
  {:result {:datik  "TEST RPC FIRED :)"
            :a {:b {:c 1
                    :d "2"}}}})


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

(defn notify-client [{connection :ws/connection} test]
  (when (and connection (server/open? connection))
    (server/send! connection (cheshire.core/generate-string test))))


(defn create-test [ctx {:keys [full-path] :as params}]
  (try
    {:result (if-let [test (ctop/select-test ctx full-path)]
               test
               (ctop/create-test ctx
                                 (ctop/update-test-status
                                  (ctop/default-test-body params) :new)))}
    (catch Exception e
      (println (ex-message e))
      {:error (ex-message e)})))


(defn get-course-tree! [ctx]
  (web.rpc/rpc-call ctx {:method 'rpc-ops/get-course-tree}))


(defmethod web.rpc/rpc 'rpc-ops/create-test [ctx {params :params}]
  (let [result (create-test ctx params)
        course-tree (get-course-tree! ctx)]
    (notify-client ctx course-tree)
    result))


(defn toggle-test [ctx {:keys [status full-path] :as params}]
  (try
    (let [status (keyword status)]
      {:result (if-let [{body :body uuid :uuid} (ctop/select-test ctx full-path)]
                 (ctop/update-test ctx uuid
                                   (ctop/update-test-status body status))
                 (ctop/create-test ctx
                                   (ctop/update-test-status
                                    (ctop/default-test-body params) status)))})
    (catch Exception e
      (println (ex-message e))
      {:error (ex-message e)})))


(defmethod web.rpc/rpc 'rpc-ops/toggle-test
  [ctx {params :params}]
  (let [result (toggle-test ctx params)
        course-tree (get-course-tree! ctx)]
    (notify-client ctx course-tree)
    result))


(defmethod web.rpc/rpc 'rpc-ops/get-course-tree
  [ctx _params]
  (let [calc-overall-module-progress (fn [course-tree]
                                       (update course-tree :modules
                                               (fn [modules]
                                                 (into {} (map (fn [[mname {:as cont,
                                                                            {:keys [total passed]
                                                                             :or {total 0
                                                                                  passed 0}} :stats}]]
                                                                 [mname (assoc-in cont [:stats :module-progress]
                                                                                  (int (* (/ passed total) 100)))]))
                                                       modules))))
        query-result (->> {:ql/type :pg/select
                           :select  :*
                           :from    :cljtest}
                          (db.query/query ctx)
                          (map #(-> (get % :body)
                                    (assoc :created_at (get % :created_at))
                                    (assoc :updated_at (get % :updated_at)))))
        course-tree (->> query-result
                         (reduce (fn [acc {:as test, :keys [module chapter test-name]}]
                                   (let [status (keyword (:status test))
                                         safe-inc (fnil inc 0)]
                                     (-> acc
                                         (assoc-in [:modules module :chapters chapter :tests test-name] test)
                                         (update-in [:stats status] safe-inc)
                                         (update-in [:stats :total] safe-inc)
                                         (update-in [:modules module :stats status] safe-inc)
                                         (update-in [:modules module :stats :total] safe-inc)
                                         (update-in [:modules module :chapters chapter :stats status] safe-inc)
                                         (update-in [:modules module :chapters chapter :stats :total] safe-inc))))
                                 {})
                         (calc-overall-module-progress))]

    {:result (clojure.walk/postwalk (fn [node]
                                      (if (map? node)
                                        (into (sorted-map) node)
                                        node))
                                    course-tree)}))


(defmethod web.rpc/rpc 'rpc-ops/get-month-activity
  [ctx _params]
  (let [query-result (db.query/plain-query ctx ["select count(*), date_trunc('day', now())::date -  date_trunc('day', updated_at)::date as offset_from_current_day
from cljtest where updated_at > (current_date - interval '5 weeks') group by date_trunc('day', now())::date -  date_trunc('day', updated_at)::date;"])
        grid-layout (into [] (repeat 35 {:activity 0}))
        complete-grid-layout (reduce (fn [acc {:keys [count offset_from_current_day]}]
                                       (assoc-in acc [offset_from_current_day :activity] count))
                                     grid-layout query-result)]

    {:result (reverse complete-grid-layout)}))
