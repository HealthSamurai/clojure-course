(ns course-content.core
  (:require [zd.zentext]
            [zd.parse]
            [zd.pages]
            [zd.methods]
            [zen.core]))


(def ztx (zen.core/new-context))


(defn get-content-path [module chapter]
  (format "content/%s/%s/chapter-content.zd" module chapter))


(defn zen-doc->html-structure [doc]
  (for [block doc]
    (or (zd.methods/render-key ztx block)
        (zd.methods/render-block ztx block))))


(defn content->html-structure [module chapter]
  (->> (get-content-path module chapter)
       slurp
       (zd.parse/parse ztx)
       :doc
       zen-doc->html-structure))
