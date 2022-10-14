(ns web.core
  (:require [org.httpkit.server :as http-kit]
            [zen.core]
            [route-map.core]
            [web.routes]
            [web.unifn :as u]))


(defn handler [{:as ctx,
                {:keys [request-method uri]} :request
                {:keys [routes]} :config}]
  (let [{:as _resolved-route
         :keys [match]}
        (route-map.core/match [request-method uri] routes)
        _ (clojure.pprint/pprint _resolved-route)
        response (:response (u/*apply match ctx))]
    response))


(defn start [config]
  (let [ztx (zen.core/new-context)
        ctx (atom {:config config
                   :zen ztx})
        _ (zen.core/read-ns ztx 'facade)
        handler-wrapper (fn [req & [opts]]
                          (handler (assoc @ctx
                                          :request req)))
        server-stop-fn (http-kit/run-server handler-wrapper (:web config))]

    (swap! ctx assoc :handler-wrapper handler-wrapper
           :server-stop-fn server-stop-fn)))

(comment
  (def server (start {:web {:port 7777}
                      :routes web.routes/routes}))

  ((:server-stop-fn server))

  )
