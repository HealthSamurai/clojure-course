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
                    :body {:result {:datik "TEST RPC FIRED :)", :a {:b {:c 1, :d "2"}}}}}))


(t/deftest simple-db-operation-test
  (world/ensure)

  (try (world/truncate :mytable)
       (catch Exception e (println (.getMessage e))))

  (world/match {:uri "/test-db"
                :request-method :get}
               {:status 200
                :body [{:id string?
                        :fields {:a "test" :b "b"}}]}))

(t/deftest simple-toggle-test
  (world/force-restart)

  (try (world/truncate :cljtest)
       (catch Exception e (println (.getMessage e))))

  (world/rpc-match {:method 'rpc-ops/toggle-test
                    :params {:ns "clojure.test"
                             :full-path "loh"
                             :module "module"
                             :chapter "chapter"
                             :file-name "file-name"
                             :test-name "one_plus_one"
                             :status :passed}}
                   {:status 200
                    :body {:result []}}))


(t/deftest course-tree-getter-test
  (try (world/truncate :cljtest)
       (catch Exception e (println (.getMessage e))))

  (world/force-restart)

  (world/rpc-match {:method 'rpc-ops/get-course-tree}
                   {:status 200
                    :body {:result {:course "tree"}}}))
