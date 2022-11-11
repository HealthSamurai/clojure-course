(ns web.core
  (:require [org.httpkit.server :as http-kit]
            [zen.core]
            [route-map.core]
            [web.routes]
            [web.unifn :as u]
            [next.jdbc :as jdbc]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [ring.util.response]
            [ring.middleware.head]
            [ring.util.codec :as codec]
            [cheshire.core :as cheshire]
            [course-tests.core]))


(defn handler [{:as ctx,
                {:keys [request-method uri]} :request
                {:keys [routes]} :config}]
  (let [{:as _resolved-route
         :keys [match]}
        (route-map.core/match [request-method uri] routes)
        response (:response (u/*apply match ctx))]
    response))


(defn handle-static [h {meth :request-method uri :uri :as req}]
  (if (and (contains? #{:get :head} meth)
           (str/starts-with? (or uri "") "/static/"))
    (let [opts {:root "public"
                :index-files? true
                :allow-symlinks? true}
          path (subs (codec/url-decode (:uri req)) 8)]
      (-> (ring.util.response/resource-response path opts)
          (ring.middleware.head/head-response req)))
    (h req)))



(defn wrap-static [h]
  (fn [req]
    (handle-static h req)))


(defn handle-body [h {body :body :as req}]
  (cond-> req
    body (assoc :body (cheshire/parse-stream (io/reader body) keyword))
    :always (h)))


(defn wrap-body [h]
  (fn [req]
    (handle-body h req)))


(defn handle-response [h req]
  (let [response (h req)]
    (update response :body (fn [body]
                             (cond-> body
                               (= (get-in response [:headers "Content-Type"]) "application/json")
                               (cheshire/generate-string ))))))


(defn wrap-response [h]
  (fn [req]
    (handle-response h req)))


(defn start [config]
  (let [ztx (zen.core/new-context)
        datasource (jdbc/get-datasource (:db config))
        ctx (atom {:config config
                   :zen ztx
                   :db datasource})
        _ (zen.core/read-ns ztx 'facade)
        handler-wrapper (-> (fn [req & [opts]]
                              (handler (assoc @ctx
                                              :request req)))
                            (wrap-static)
                            (wrap-body)
                            (wrap-response))
        server-stop-fn (http-kit/run-server handler-wrapper (:web config))]
    (future
      (try (course-tests.core/update-test-list @ctx)
           (println :done-rescan-tests)
           (catch Exception e
             (println e))))
    (swap! ctx assoc :handler-wrapper handler-wrapper
           :server-stop-fn server-stop-fn)))

(comment

  (def server (start {:web {:port 7777}
                      :routes web.routes/routes
                      :db {:dbtype "postgres"
                           :dbname "course"
                           :host "localhost"
                           :port 5444
                           :user "course"
                           :password "password"}}))

  ((:server-stop-fn server))

  )
