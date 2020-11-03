(ns odmp-ui.util.stream
  (:require [re-frame.core :as rf]
            [odmp-ui.util.network :as net]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            ["/fetch_event_source/fetch" :as es]))

(defn start-stream [db]
  (let [headers (net/basic-headers db)]
    (go
      (es/fetchEventSource
        "dataflow_api/event/stream"
        (clj->js
          {:headers headers
           :onmessage (fn [ev] (println ev.data))
           :onclose (fn [] (println "CLOSED"))
           :onerror (fn [err] (println err))})))))

(rf/reg-event-fx
  ::start-event-stream
  (fn [{:keys [db]}]
    (start-stream db)
    {:db db}))