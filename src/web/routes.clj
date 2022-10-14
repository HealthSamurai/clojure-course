(ns web.routes
  (:require [web.operations]))

(def routes
  {"rpc" {:POST :web.operations/rpc}
   :GET :web.operations/root
   "test-db" {:GET :web.operations/test-db}})
