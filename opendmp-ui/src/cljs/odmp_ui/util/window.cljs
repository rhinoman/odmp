(ns odmp-ui.util.window
  (:require [re-frame.core :as rf]
            [goog.events :as events])
  (:import [goog.events]))


;; JS Window events and such

(def ^:dynamic *listeners* (atom {}))

(def resize-debounce-ms 50)

(defn on-resize [timer]
  (js/clearTimeout @timer)
  (reset! timer
          (js/setTimeout
           #(rf/dispatch [::resized js/window.innerWidth js/window.innerHeight])
           resize-debounce-ms)))

(rf/reg-fx
 ::on-resize
 (fn [db [_ _]]
   (println "START ON RESIZE")
   (let [timer (atom nil)
         listener (events/listen
                   js/window events/EventType.RESIZE
                   #(on-resize timer))]
     (swap! *listeners* assoc-in [events/EventType.RESIZE ::window-resize-listener] listener))))

(rf/reg-event-fx
 ::start-on-resize
 (fn [db [_ _]]
   {::on-resize {}}))

(rf/reg-event-fx
 ::stop-on-resize
 (fn []
   {::stop-resize {}}))

(rf/reg-fx
 ::stop-resize
 (fn [db [_ _]]
   (println "STOPPING!")
   (when-let [listener (get-in @*listeners* [events/EventType.RESIZE ::window-resize-listener])]
     (events/unlistenByKey listener)
     (swap! *listeners* update events/EventsType.RESIZE #(dissoc % ::window-resize-listener)))))

(rf/reg-event-db
 ::resized
 (fn [db [_ width height]]
   (-> db
       (assoc-in [:window :size :width] width)
       (assoc-in [:window :size :height] height))))

(rf/reg-sub
 ::resize
 (fn [db _]
   (get-in db [:window :size])))
