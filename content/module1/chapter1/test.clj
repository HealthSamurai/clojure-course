(ns module1.chapter1.test
  (:require  [clojure.test :as t]))


(def _ nil)


(t/deftest equation-stuff
  (t/is (= 1 _)))


;; cljtest - table
;; id - uuid
;; body - jsonb
;; ns - module1.chapter1
;; deftest name
;; passed count
;; failed count
;; status - passed / failed
