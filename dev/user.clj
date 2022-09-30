(ns user
  (:require shadow.cljs.devtools.api
            shadow.cljs.devtools.server))

(defn restart-ui
  []
  (shadow.cljs.devtools.server/stop!)
  (shadow.cljs.devtools.server/start!)
  (shadow.cljs.devtools.api/watch :frontend))

(comment
  (restart-ui)
  )
