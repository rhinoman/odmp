(ns odmp-dataflow.db.flow
  (:require [somnium.congomongo :as mg]
            [odmp-dataflow.db.mongo :refer [wrap-mongo-request]]))


(defn get-list [{:keys [db log]}]
  (mg/with-mongo db
                 (wrap-mongo-request #(mg/fetch :dataflows) log)))

(defn lookup [id {:keys [db log]}]
  (mg/with-mongo db
                 (wrap-mongo-request #(mg/fetch-by-id :dataflows (mg/object-id id)) log)))


(defn create [new-flow])

