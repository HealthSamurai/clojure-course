(ns clojure-course.course-tree.view
  (:require [clojure-course.pages]
            [clojure-course.course-tree.model :refer [course-tree-s]]
            [zf]
            [stylo.core :refer [c]]))


(zf/defv course-tree [course-tree-s]
  [:div (pr-str course-tree-s)])


(clojure-course.pages/reg-page clojure-course.course-tree.model/page course-tree)
