(ns clojure-course.activity-tracker.model
  (:require [zf]))


(def page ::index )


(zf/defx open-activity-tracker
  [{:keys [db]} & _]
  {:db (assoc-in db [page :activity-tracker-open?] true)
   :zen/rpc {:method 'rpc-ops/get-month-activity
             :path [page]}})

(zf/defx close-activity-tracker
  [{:keys [db]} & _]
  {:db (assoc-in db [page :activity-tracker-open?] false)})

(zf/defs activity-tracker-s
  [db _]
  (get-in db [page :data]))

(zf/defs last-action
  [db _]
  (get-in db [:clojure-course.course-tree.model/index :data :last-action :body]))

(zf/defs activity-tracker-open?
  [db _]
  (get-in db [page :activity-tracker-open?]))
