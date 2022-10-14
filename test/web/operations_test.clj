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


(t/deftest simple-db-operation-test
  (world/ensure)

  (try (world/truncate :mytable)
       (catch Exception e (println (.getMessage e))))

  (world/match {:uri "/test-db"
                :request-method :get}
               {:status 200
                :body [{:id string?
                        :fields {:a "test" :b "b"}}]})



  )

(t/deftest simple-toggle-test
  (world/ensure)

  (try (world/truncate :cljtest)
       (catch Exception e (println (.getMessage e))))

  (world/rpc-match {:method 'rpc-ops/toggle-test
                    :params {:ns "clojure.test"
                             :test-name "one_plus_one"
                             :status :passed}}
                   {:status 200
                    :body {:result "OK"}})


  )
