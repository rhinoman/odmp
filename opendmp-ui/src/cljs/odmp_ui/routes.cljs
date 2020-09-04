;; Copyright 2020 James Adam and the Open Data Management Platform contributors.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns odmp-ui.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import [goog History]
           [goog.history EventType])
  (:require
   [secretary.core :as secretary]
   [goog.events :as gevents]
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]
   [odmp-ui.events :as events]
   [odmp-ui.util.window :as window]
   [goog.history.EventType :as EventType]))


(defn location []
  (.-location js/window))

(defn hook-browser-navigation! []

;; Quick and dirty history configuration.
(let [h (History.)]
  (gevents/listen h EventType/NAVIGATE #(secretary/dispatch! ^js (.-token %)))
  (doto h (.setEnabled true))
  ;; location will have to be implemented using (.-location js/window)
  (when (empty? (.-hash (location)))
      (set! (.-hash (location)) "#/")))
)


(defn app-routes []
  (secretary/set-config! :prefix "#")
  (re-frame/dispatch [::window/start-on-resize])
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (re-frame/dispatch [::events/set-active-panel :home-panel])
    (re-frame/dispatch [::events/set-active-sidebar-link :home]))

  (defroute "/about" []
    (re-frame/dispatch [::events/set-active-panel :about-panel]))

  (defroute "/dataflows" []
    (re-frame/dispatch [::events/set-active-panel :dataflow-index-panel])
    (re-frame/dispatch [::events/set-active-sidebar-link :dataflows]))

  (defroute "/dataflows/:id" [id]
    (println id)
    (re-frame/dispatch [::events/set-active-panel :dataflow-item-panel id])
    (re-frame/dispatch [::events/set-active-sidebar-link :dataflows]))

  (defroute "/processors/:id" [id]
    (re-frame/dispatch [::events/set-active-panel :processor-item-panel id])
    (re-frame/dispatch [::events/set-active-sidebar-link :dataflows]))

  (defroute "/collections" []
    (re-frame/dispatch [::events/set-active-panel :collection-index-panel])
    (re-frame/dispatch [::events/set-active-sidebar-link :collections]))

  (defroute "/collections/:id" [id]
    (re-frame/dispatch [::events/set-active-panel :collection-item-panel id])
    (re-frame/dispatch [::events/set-active-sidebar-link :collections]))


  ;; --------------------
  (hook-browser-navigation!))
