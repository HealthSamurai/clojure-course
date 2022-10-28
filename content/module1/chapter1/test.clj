(ns module1.chapter1.test
  (:require  [clojure.test :as t]
             [retest.retest :as rt]
             [clojure.stacktrace]))


(def _ nil)


(t/deftest simple-failed
  (t/testing "simple test"
    (t/is (= 1 1))))


(t/deftest simple-error
  (t/testing "simple test"
    (t/is (= 1 1))))


(t/deftest simple-passed
  (t/testing "simple test"
    (t/is (= 1 2))))



;; cljtest - table
;; id - uuid
;; body - jsonb
;; ns - module1.chapter1
;; deftest name
;; passed count
;; failed count
;; status - passed / failed
