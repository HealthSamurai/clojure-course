(ns clojure-course.course-content.model
  (:require [zf]))


(def page ::index)


(zf/defx fetch-content
  [_ module chapter]
  {:zen/rpc {:method 'rpc-ops/get-course-content
             :params {:module module
                      :chapter chapter}
             :path [page]}})


(zf/defs selected-content
  [db]
  (get-in db [page :data]))
