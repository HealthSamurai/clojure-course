(ns module1.chapter1.simple-test
  (:require  [clojure.test :as t]
             [retest.retest :as rt]))



(rt/deftest simple-failed
  (t/testing "simple test"
    (t/is (= 1 1))))


(rt/deftest simple-error
  (t/testing "simple test"
    (t/is (= 1 1))))


(rt/deftest simple-passed
  (t/testing "simple test"
    (t/is (= 1 2))))
