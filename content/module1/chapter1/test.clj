(ns module1.chapter1.test
  (:require  [clojure.test :as t]
             [retest.retest :as rt]
             [clojure.stacktrace]))


(def _ nil)


(rt/deftest simple-test2
  (t/testing "simple test"
    (t/is (= 1 _))))


;; cljtest - table
;; id - uuid
;; body - jsonb
;; ns - module1.chapter1
;; deftest name
;; passed count
;; failed count
;; status - passed / failed
