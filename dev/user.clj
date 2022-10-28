(ns user
  (:require [shadow.cljs.devtools.api :as shadow]
            [shadow.cljs.devtools.config :as shadow.config]
            [shadow.cljs.devtools.server :as shadow.server]
            [clojure.java.io :as io]
            [web.core]
            [web.routes]))


(defn delete-recursively [f]
  (when (.isDirectory ^java.io.File f)
    (doseq [c (.listFiles ^java.io.File f)]
      (delete-recursively c)))
  (io/delete-file f))


(defn restart-ui [& [clean]]
  (let [cfg (shadow.config/load-cljs-edn!)]
    (shadow.server/stop!)
    (let [f (io/file ".shadow-cljs")]
      (when (.exists f)
        (delete-recursively f)))
    (when clean
      (doseq [[bid _] (:builds cfg)]
        (when-not (= :npm bid)
          (try (-> (shadow.config/get-build bid)
                   (get-in [:dev :output-dir])
                   (io/file)
                   (delete-recursively))
               (catch Exception _)))))
    (shadow.server/start!)
    (doseq [[bid _] (:builds cfg)]
      (println "BID>" bid)
      (when-not (= :npm bid)
        (shadow/watch bid)))))

(comment
  (restart-ui)

  (def server (web.core/start {:web {:port 7777}
                               :routes web.routes/routes
                               :db {:dbtype "postgres"
                                    :dbname "course"
                                    :host "localhost"
                                    :port 5444
                                    :user "course"
                                    :password "password"}}))

  ((:server-stop-fn server))

  )
