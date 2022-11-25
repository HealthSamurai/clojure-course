(ns web.routes
  (:require [web.operations]))

(def routes
  {"rpc" {:POST :web.operations/rpc}
   :GET :web.operations/root
   "ws" {:GET :web.operations/websocket}
   "test-db" {:GET :web.operations/test-db}})
