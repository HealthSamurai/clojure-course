(ns clojure-course.course-tree.model
  (:require [zf]))


(def page ::index )


(zf/defx course-tree-init
  [{:keys [db]} & _]
  {:zen/rpc {:method 'rpc-ops/get-course-tree
             :path [page]}})


(zf/defx select-module
  [{:keys [db]} module]
  {:db (assoc-in db [page :selected-module] module)})


(zf/defs selected-module
  [db _]
  (get-in db [page :selected-module]))


(zf/defx select-chapter
  [{:keys [db]} module]
  {:db (assoc-in db [page :selected-chapter] module)})


(zf/defs selected-chapter
  [db _]
  (get-in db [page :selected-chapter]))


(zf/defs course-tree-s
  [db _]
  (get-in db [page :data]))


(zf/defx index [{db :db} phase _opts]
  (case phase
    :init {:dispatch [::course-tree-init]}
    :params nil
    :deinit nil
    nil))
