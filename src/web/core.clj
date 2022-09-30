(ns web.core
  (:require
   [clojure.string :as str]
   [org.httpkit.server :as http-kit]
   [web.formats]
   [clojure.walk]
   [ring.middleware.content-type]
   [ring.middleware.cookies :as cookies]
   [ring.middleware.head]
   [ring.middleware.not-modified]
   [ring.util.codec :as codec]
   [ring.util.request]
   [ring.util.response]
   [ring.middleware.resource]
   [ring.middleware.file])
  (:import [org.httpkit PrefixThreadFactory]
           [java.util.concurrent ArrayBlockingQueue ThreadPoolExecutor TimeUnit]))

(defn form-decode [s] (clojure.walk/keywordize-keys (ring.util.codec/form-decode s)))

(defn parse-header [header]
  (into {}
        (map (fn [kv]
               (let [[k v] (str/split kv #"=" 2)]
                 {(-> k str/trim str/lower-case keyword)
                  (or (some-> v str/trim str/lower-case)
                      true)})))
        (str/split (str header) #",")))

(defn prepare-request [{meth :request-method qs :query-string _body :body _ct :content-type headers :headers :as req} & [config]]
  (let [params (when qs (form-decode qs))
        params (if (string? params)
                 (if (clojure.string/blank? params)
                   {}
                   {(keyword params) nil})
                 params)
        method-override (and (= :post meth) (get headers "x-http-method-override"))
        request-without-body? (contains? #{:get :delete} meth)
        body (when-not request-without-body?
               (web.formats/parse-body req config))
        uri (codec/url-decode (:uri req))]
    (cond-> req
      body (merge body)
      request-without-body? (dissoc :body)
      method-override (assoc :request-method (keyword (str/lower-case method-override)))
      params (update :params merge (or params {}))
      :always (assoc :uri uri))))

(defn preflight
  [{_meth :request-method hs :headers :as _req}]
  (let [headers (get hs "access-control-request-headers")
        origin (get hs "origin")
        meth  (get hs "access-control-request-method")]
    {:status 200
     :headers {"Access-Control-Allow-Headers" headers
               "Access-Control-Allow-Methods" meth
               "Access-Control-Allow-Origin" origin
               "Access-Control-Allow-Credentials" "true"
               "Access-Control-Expose-Headers" "Location, Transaction-Meta, Content-Location, Category, Content-Type, X-total-count, X-Request-Id"}}))

(defn allow [resp req]
  (if-let [origin (get-in req [:headers "origin"])]
    (update resp :headers merge
            {"Access-Control-Allow-Origin" origin
             "Access-Control-Allow-Credentials" "true"
             "Access-Control-Expose-Headers" "Location, Content-Location, Category, Content-Type, X-total-count, X-Request-Id"})
    resp))

(defn healthcheck [h]
  (fn [req]
    (if  (and (= :get (:request-method req))
              (= "/__healthcheck" (:uri req)))
      {:status 200 :body "healthy" :headers {"content-type" "text/html"}}
      (h req))))

(defn wrap-prefer [h]
  (fn [req]
    (let [resp          (h req)
          prefer-header (parse-header (get-in req [:headers "prefer"] "return=representation"))]
      (case (:return prefer-header)
        "minimal" (assoc resp :body {})
        resp))))

(defn mk-handler [dispatch & [config]]
  (fn [req]
    (if (= :options (:request-method req))
      (preflight req)
      (let [req (prepare-request req config)
            resp (dispatch req)]
        (-> resp
            (web.formats/format-response req)
            (allow req))))))

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

(defn handler [dispatch & [config]]
  (-> (mk-handler dispatch config)
      ;;wrap-prefer
      healthcheck
      (cookies/wrap-cookies)
      (wrap-static)
      (ring.middleware.content-type/wrap-content-type {:mime-types {nil "text/html"}})
      ring.middleware.not-modified/wrap-not-modified))


(defn start
  "start server with dynamic metadata"
  ([config dispatch]
   (start nil config dispatch))
  ([ztx config dispatch]
   ;; todo this should not start a web
   (let [web-config (merge {:port               8080
                            :worker-name-prefix "w"
                            :thread 8 ;; this param will be overwritten by merge.
                            :max-body 20971520} config )
         web-config (update web-config :port (fn [x] (if (string? x) (Integer/parseInt x) x)))]
     (println :http/start web-config)
     (let [prefix-thread-factory (PrefixThreadFactory. (:worker-name-prefix web-config))
           array-blocking-queue  (ArrayBlockingQueue. (or (:queue-size web-config) 20480))
           thread-pool-executor  (ThreadPoolExecutor. #_"core pool size:" (:thread web-config)
                                                                          #_"max pool size:" (:thread web-config)
                                                                          #_"keep alive time:" 0
                                                                          TimeUnit/MILLISECONDS
                                                                          array-blocking-queue
                                                                          prefix-thread-factory)]
       (when ztx
         (swap! ztx assoc :web/thread-pool thread-pool-executor))
       (http-kit/run-server (handler dispatch config) (assoc web-config :worker-pool thread-pool-executor))))))


(defn stop [server]
  (server))
