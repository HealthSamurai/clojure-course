(ns retest.retest-test
  (:require [clojure.test :as t]
            [retest.retest :as rt]
            [matcho.core :as matcho]
            [org.httpkit.client :as client]))



(t/deftest retest-report-test

  (let [report-data (atom [])]
    (binding [t/report (partial swap! report-data conj)]
      (rt/retest-report (partial swap! report-data conj)
                        {:test-ns 'module.chapter.file
                         :test-name 'name}
                        {:type :passed}))
    (matcho/match
     @report-data
     [{:type :retest-passed
       :module "module"
       :chapter "chapter"
       :file-name "file"
       :test-name "name"
       :full-path "module.chapter.file/name"}
      {:type :passed}
      nil]))

  (let [report-data (atom [])]
    (binding [t/report (partial swap! report-data conj)]
      (rt/retest-report (partial swap! report-data conj)
                        {:test-ns 'module
                         :test-name 'name}
                        {:type :passed}))
    (matcho/match
     @report-data
     [{:type :retest-passed
       :module "module"
       :chapter nil
       :file-name nil
       :test-name "name"
       :full-path "module/name"}
      {:type :passed}
      nil]))

  (let [report-data (atom [])]
    (binding [t/report (partial swap! report-data conj)]
      (rt/retest-report (partial swap! report-data conj)
                        {:test-ns nil
                         :test-name nil}
                        {:type :passed}))
    (matcho/match
     @report-data
     []))

  )

(t/deftest send-report-test
  (with-redefs [org.httpkit.client/post
                (fn [url payload]
                  (matcho/match
                   payload
                   {:headers {"content-type" "application/json"}
                    :body (str "{\"method\":\"rpc-ops/toggle-test\","
                               "\"params\":{\"type\":\"passed\"}}")})
                  (future 0))]
    (rt/send-report! {:type :passed})))

(t/deftest send-create-test
  (with-redefs [org.httpkit.client/post
                (fn [url payload]
                  (matcho/match
                   payload
                   {:headers {"content-type" "application/json"}
                    :body (str "{\"method\":\"rpc-ops/create-test\","
                               "\"params\":{\"type\":\"new\"}}")})
                  (future 0))]
    (rt/create-test! {:type :new})))


(t/deftest make-report-event-test
  (matcho/match
   (rt/make-report-event {:module "module"
                          :chapter "chapter"
                          :file-name "filename"
                          :test-name "testname"
                          :full-path "mudule.chapter.filename/testname"}
                         :error)
   {:status :error
    :module "module"
    :chapter "chapter"
    :file-name "filename"
    :test-name "testname"
    :full-path "mudule.chapter.filename/testname"}))
