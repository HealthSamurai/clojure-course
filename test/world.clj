(ns world
  (:require [web.core]
            [web.routes]
            [matcho.core]
            [db.query]
            [cheshire.core]))


(defonce servers (atom {}))


(defn nuke-world! []
  (-> servers
      deref
      vals
      (->> (mapv (fn [s] ((:server-stop-fn s))))))

  (reset! servers {}))


(def default-config
  {:id     "test-server"
   :web    {:port 27999}
   :routes web.routes/routes
   :db {:dbtype "postgres"
        :dbname "course"
        :host "localhost"
        :port 5444
        :user "course"
        :password "password"}})


(defn ensure
  ([] (ensure default-config))
  ([{:as cfg, :keys [id]
     :or {id "test-server"}}]
   (when-not
       (get @servers id)
     (swap! servers assoc id (web.core/start cfg)))))


(defn force-restart
  ([] (force-restart default-config))
  ([{:as cfg, :keys [id]
     :or {id "test-server"}}]
   (try ((get-in @servers [id :server-stop-fn]))
        (catch Exception e (.getMessage e))
        (finally (swap! servers assoc id (web.core/start cfg))))))


(defn rpc-match
  ([req match] (rpc-match (get @servers "test-server") req match))
  ([{:as cfg, {:keys [id]} :config} req match]
   (let [ctx (get @servers id)
         handler (get ctx :handler-wrapper)]
     (matcho.core/match
       (update (handler {:uri "/rpc"
                         :request-method :post
                         :body req})
               :body #(cheshire.core/parse-string % keyword))
       match))))

(defn match
  ([req match'] (match (get @servers "test-server") req match'))
  ([{:as cfg, {:keys [id]} :config} req match']
   (let [ctx (get @servers id)
         handler (get ctx :handler-wrapper)]
     (let [res (handler req)]
       (matcho.core/match
        res
        match')
       res))))


(defn truncate
  ([table-name] (truncate (get @servers "test-server") table-name))
  ([ctx table-name] (db.query/query ctx {:ql/type :pg/delete
                                         :from    table-name})))
