(ns web.rpc
  (:require [zen.core :as zen]
            [zen.v2-validation :as v2]))

(defmulti rpc (fn [_ctx req] (or (:engine req) (symbol (:method req)))))

(defn rpc-call [{zen :zen :as ctx}
                {:as rpc-req, id :id meth :method params :params }]
  (let [res (let [op-name     (and meth (symbol meth))
                  op-def      (zen/get-symbol zen op-name)
                  op-engine    (:engine op-def)]

              (if (nil? op-def)
                {:error {:message (str "No definition for " meth)}}
                (if (contains? (:zen/tags op-def) 'core/rpc)
                  (if-let [errs (when-let [prm-schema (:params op-def)]
                                  (let [{errs :errors} (v2/validate-schema zen prm-schema params)]
                                    (when-not (empty? errs)
                                      errs)))]
                    {:error {:message "Invalid params"
                             :method meth
                             :errors errs}}
                    (if op-engine
                      (rpc ctx (assoc rpc-req :engine op-engine, :definition op-def))
                      (rpc ctx (assoc rpc-req :definition op-def))))
                  {:error {:message (str op-name " is not tagged with core/rpc, " op-def)}})))]
    (if (and id (map? res))
      (assoc res :id id)
      res)))
