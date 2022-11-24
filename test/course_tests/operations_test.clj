(ns course-tests.operations-test
  (:require [clojure.test :as t]
            [world]
            [course-tests.operations :as operations]
            [cheshire.core :as cheshire]
            [matcho.core :as matcho]
            [db.query :as db]))





(t/deftest update-test-list-status-test
  (world/ensure)

  (world/truncate :cljtest)

  (operations/create-test (get @world/servers "test-server")
                          {:module "module"
                           :chapter "chapter"
                           :file-name "filename"
                           :test-name "testname1"
                           :full-path "module.chapter.filename/testname1"
                           :status "passed"})

  (operations/create-test (get @world/servers "test-server")
                          {:module "module"
                           :chapter "chapter"
                           :file-name "filename"
                           :test-name "testname2"
                           :full-path "module.chapter.filename/testname2"
                           :status "passed"})

  (operations/update-test-list-status
   (get @world/servers "test-server")
   [{:module "module"
     :chapter "chapter"
     :file-name "filename"
     :test-name "testname1"
     :full-path "module.chapter.filename/testname1"
     :status "passed"}])

  (matcho/match
   (db/query (get @world/servers "test-server")
             {:ql/type :pg/select
              :select :*
              :from :cljtest})
   [{:body {:module "module"
            :chapter "chapter"
            :file-name "filename"
            :test-name "testname1"
            :full-path "module.chapter.filename/testname1"
            :status "passed"}}])

  (operations/update-test-list-status
   (get @world/servers "test-server")
   [{:module "module"
     :chapter "chapter"
     :file-name "filename"
     :test-name "testname1"
     :full-path "module.chapter.filename/testname1"
     :status "passed"}
    {:module "module"
     :chapter "chapter"
     :file-name "filename"
     :test-name "testname2"
     :full-path "module.chapter.filename/testname2"
     :status "passed"}])


  (matcho/match
   (db/query (get @world/servers "test-server")
             {:ql/type :pg/select
              :select :*
              :from :cljtest})
   [{:body {:module "module"
            :chapter "chapter"
            :file-name "filename"
            :test-name "testname1"
            :full-path "module.chapter.filename/testname1"
            :status "passed"}}
    {:body {:module "module"
            :chapter "chapter"
            :file-name "filename"
            :test-name "testname2"
            :full-path "module.chapter.filename/testname2"
            :passed 0
            :error 0
            :status "new"}}]))
