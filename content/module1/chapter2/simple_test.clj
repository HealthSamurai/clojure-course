(ns module1.chapter2.simple-test
  (:require [clojure.test :as t]))






(t/deftest chapter2-test
  (t/testing "simple test"
    (t/is (= 1 1))))
