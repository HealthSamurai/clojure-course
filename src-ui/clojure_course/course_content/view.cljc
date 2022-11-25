(ns clojure-course.course-content.view
  (:require [clojure-course.pages]
            [clojure-course.course-content.model :refer [selected-content]]
            [zf]))


(zf/defv course-content [selected-content]
  [:div.course-content
   {:dangerouslySetInnerHTML
    {:__html selected-content}}])


(clojure-course.pages/reg-page clojure-course.course-tree.model/page course-content)
