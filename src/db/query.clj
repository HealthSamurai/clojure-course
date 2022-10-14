(ns db.query
  (:require [next.jdbc :as jdbc]
            [dsql.core]))

(defn query [{:as _ctx, :keys [db]} query]
  (jdbc/execute! db (dsql.core/format {} query)))
