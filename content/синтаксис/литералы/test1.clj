(ns синтаксис.литералы.abc-test
  (:require  [clojure.test :as t]
             [retest.retest :as rt]
             [clojure.spec.alpha]
             [clojure.string]))

(rt/deftest string-test
  (def my nil)

  (t/is (string? my)))

(rt/deftest sub-string-test
  (def my nil)

  (t/is (= (subs "Hello Clojure" 6)
             my)))

(rt/deftest map-test
  (def my nil)

  (t/is (map?
         my)))

(rt/deftest map-assoc-test
  (def my nil)

  (t/is (= (-> {}
               (assoc :two (+ 1 1))
               (assoc :number (/ 50 5 2 5)))
           my)))

(rt/deftest vector-test
  (def my nil)

  (t/is (vector? my)))

(rt/deftest vector-conj-test
  (def my nil)

  (t/is (= (conj [1] 2 :three 'four \c true "seven")
           my)))

(rt/deftest list-test
  (def my nil)

  (t/is (list? my)))

(rt/deftest list-conj-test
  (def my nil)

  (t/is (= (conj '(1) 2 :three 'four \c true "seven")
           my)))

(rt/deftest regex-test
  (def my nil)

  (t/is (clojure.spec.alpha/regex? my)))

(rt/deftest re-find-test
  (def my nil)

  (t/is (= (re-find #"m..n" "Fly me to the moon")
           my)))

(rt/deftest symbol-test
  (def my nil)

  (t/is (symbol? my)))

(rt/deftest symbol-conversion-test
  (def my nil)

  (t/is (= (symbol (str 'Who (name :am) 'I?))
           my)))

(rt/deftest keyword-test
  (def my nil)

  (t/is (keyword? my)))

(rt/deftest keyword-conversion-test
  (def my nil)

  (t/is (= (keyword (str (name :Who) (symbol "am") 'I?))
           my)))

(rt/deftest set-test
  (def my nil)

  (t/is (set? my)))

(rt/deftest set-order-test
  (def my nil)

  (t/is (= (set [5 4 3 2 1])
           my)))
