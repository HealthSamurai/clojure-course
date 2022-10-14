(ns clojure-course.interop)

(defn get-location-hash
  []
  #?(:cljs (.. js/window -location -hash)
     :clj  ""))
