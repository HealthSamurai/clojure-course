(ns course.core
  (:require zd.core
            zd.methods
            zen.core))

(defmethod zd.core/op ::index-page
  [_ztx _op _req]
  {:status  302
   :headers {"Location" "/introduction"}})

(def routes
  {:GET {:op ::index-page}})

(defn start
  []
  (let [ztx (zen.core/new-context {:zd/paths ["docs"] :paths []})]
    (zd.core/start ztx {:port 3030 :route-map routes})
    ztx))

(comment
  (def ztx (start))
  (zd.core/stop ztx))
