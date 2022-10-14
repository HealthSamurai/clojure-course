(ns web.operations-test
  (:require [web.operations :as sut]
            [clojure.test :as t]
            [world]))

(t/deftest simple-rpc-op-test
  (world/ensure)

  (world/rpc-match {:method 'rpc-ops/test-rpc
                    :params {:a 1
                             :b 2}}
                   {:status 200
                    :body {:result "TEST RPC FIRED :)"}}))
