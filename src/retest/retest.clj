(ns retest.retest
  (:require [clojure.test :as t]
            [clojure.string :as str]
            [org.httpkit.client :as client]
            [cheshire.core :as cheshire]))

(def ^:dynamic *update-test-list* nil)

(defn retest-report [f {:keys [test-ns test-name]} m]
  (let [[module chapter file-name] (str/split (name test-ns) #"\." 3)
        full-path (str test-ns \/ test-name)]
    (t/report (-> m
                  (update :type #(->> % name (str "retest-") keyword))
                  (assoc :module module
                         :chapter chapter
                         :file-name file-name
                         :test-name (name test-name)
                         :full-path full-path))))
  (f m))


(defn send-report! [m]
  ;; TODO: get port from config
  @(client/post "http://localhost:7777/rpc"
                {:headers {"content-type" "application/json"}
                 :body (cheshire/generate-string
                        {:method 'rpc-ops/toggle-test
                         :params m})}))

(defn create-test! [m]
  ;; TODO: get port from config
  @(client/post "http://localhost:7777/rpc"
                {:headers {"content-type" "application/json"}
                 :body (cheshire/generate-string
                        {:method 'rpc-ops/create-test
                         :params m})}))

(defn make-report-event [m status]
  (assoc (select-keys m [:module :chapter :file-name :test-name :full-path])
         :status status))

(defn add-report-methods []
  (defmethod t/report :retest-pass [m]
    (send-report! (doto (make-report-event m :passed)
                    (prn '!>>>>))))

  (defmethod t/report :retest-fail [m]
    (send-report! (make-report-event m :failed)))

  (defmethod t/report :retest-error [m]
    (send-report! (make-report-event m :error)))

  (defmethod t/report :retest-summary [m]
    (println m))

  (defmethod t/report :retest-begin-test-ns [m]
    (println m))

  ;; Ignore these message types:
  (defmethod t/report :retest-end-test-ns [m]
    (println m))
  (defmethod t/report :retest-begin-test-var [m]
    (println m))
  (defmethod t/report :retest-end-test-var [m]
    (println m)))



(defmacro deftest
  "Defines a test function with no arguments.  Test functions may call
  other tests, so tests may be composed.  If you compose tests, you
  should also define a function named test-ns-hook; run-tests will
  call test-ns-hook instead of testing all vars.

  Note: Actually, the test body goes in the :test metadata on the var,
  and the real function (the value of the var) calls test-var on
  itself.

  When *load-tests* is false, deftest is ignored."
  {:added "1.1"}
  [name & body]
  (let [cur-ns (clojure.core/name (ns-name *ns*))
        [module chapter file-name] (str/split cur-ns #"\." 3)
        full-path (str cur-ns \/ name)
        test-info {:module module
                   :chapter chapter
                   :file-name file-name
                   :test-name name
                   :full-path full-path}]
    (if *update-test-list*
      (swap! *update-test-list* conj test-info)
      (create-test! test-info)))
  (when t/*load-tests*
      `(def ~(vary-meta name assoc :test
                        `(fn []
                           (let [f# ~t/do-report
                                 m# (meta (var ~name))]
                             (add-report-methods)
                             (alter-var-root
                              ~(var t/do-report)
                              (constantly
                               (partial retest-report
                                        f#
                                        (hash-map :test-ns (ns-name (:ns m#))
                                                  :test-name (:name m#)))))
                             ~@body
                             (alter-var-root ~(var t/do-report)
                                             (constantly f#)))))
         (fn [] (t/test-var (var ~name))))))

(defmacro deftest-
  "Like deftest but creates a private var."
  {:added "1.1"}
  [name & body]
  (println name)
  (when t/*load-tests*
    `(def ~(vary-meta name assoc :test `(fn [] (println ~name) ~@body) :private true)
       (fn [] `(let [f t/report]
               (binding [t/report (partial retest-report f)]
                 (t/test-var (var ~name))))))))
