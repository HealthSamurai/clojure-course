(ns clojure-course.dev
  (:require [clojure-course.index :as core]
            [devtools.core]))

(devtools.core/install!)

(defn ^:dev/after-load re-render []
  (core/mount-root))

(core/init!)
