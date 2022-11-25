(ns course-tests.core
  (:require [retest.retest :as rt]
            [db.query :as db]
            [clojure.java.io :as io]
            [course-tests.operations :as ctop]
            [clojure.string :as str]))


(defn recursive-eval [file]
  (let [file (io/file file)
        absolute-path (.getAbsolutePath file)]
    (cond
      (and (not (.exists file))
           (not (.canRead file))) nil

      (.isDirectory file)
      (doseq [child-file (.list file)]
        (when (str/ends-with? child-file ".clj")
          (recursive-eval (str absolute-path \/ child-file))))

      (.isFile file)
      (load-file absolute-path))))


(defn create-cljtest-table [ctx]
  (db/query
   ctx
   {:ql/type :pg/create-table
    :table-name :cljtest
    :if-not-exists true
    :unlogged true
    :columns {:uuid {:type "text" :primary-key true}
              :created_at {:type "date"}
              :updated_at {:type "date"}
              :body {:type "jsonb"}}}))

(defn update-test-list [ctx]
  (binding [rt/*update-test-list* (atom [])]
    (recursive-eval "content")
    (create-cljtest-table ctx)
    (course-tests.operations/update-test-list-status ctx @rt/*update-test-list*)))
