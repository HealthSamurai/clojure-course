(ns module1.chapter1.test
  (:require  [clojure.test :as t]))


(def _ nil)


(defmethod t/report :testik [m]
  (println "M" m))


(t/deftest equation-stuff
  (t/do-report {:type     :testik})
  (t/is (= 1 _) )


  )

t/*test-out*

;; cljtest - table
;; id - uuid
;; body - jsonb
;; ns - module1.chapter1
;; deftest name
;; passed count
;; failed count
;; status - passed / failed
