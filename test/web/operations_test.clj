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
                   {:status 200,
                    :body
                    {:result
                     {:module1
                      {:chapter1
                       {:simple-failed
                        {:passed 0,
                         :full-path "module1.chapter1.test/simple-failed",
                         :module "module1",
                         :chapter "chapter1",
                         :updated_at string?,
                         :status "new",
                         :file-name "test",
                         :test-name "simple-failed",
                         :error 0,
                         :created_at string?,
                         :failed 0},
                        :simple-error
                        {:passed 0,
                         :full-path "module1.chapter1.test/simple-error",
                         :module "module1",
                         :chapter "chapter1",
                         :updated_at string?,
                         :status "new",
                         :file-name "test",
                         :test-name "simple-error",
                         :error 0,
                         :created_at string?,
                         :failed 0}},
                       :chapter2
                       {:chapter2-test
                        {:passed 0,
                         :full-path "module1.chapter2.test/chapter2-test",
                         :module "module1",
                         :chapter "chapter2",
                         :updated_at string?,
                         :status "new",
                         :file-name "test",
                         :test-name "chapter2-test",
                         :error 0,
                         :created_at string?,
                         :failed 0}}}}},
                    :headers {"Content-Type" "application/json"}}))
