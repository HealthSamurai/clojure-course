(ns web.operations
  (:require [web.unifn :as u]
            [web.rpc]))


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
