;; Copyright 2020 The Open Data Management Platform contributors.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns odmp-ui.events
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [odmp-ui.db :as db]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [promesa.core :as p]
   [secretary.core :as secretary]
   ["keycloak-js" :as Keycloak ]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))


(re-frame/reg-event-db
 ::login-finished
 (fn [db [_ result]]
   (secretary/dispatch! (-> js/window .-location .-hash))
   (assoc-in db [:auth-state :authenticated] result)))

(re-frame/reg-event-db
 ::initialize-keycloak
  (fn [db [_ _]]
    (let [keycloak (Keycloak "/assets/keycloak.json")]
      (-> keycloak
          (.init #js{:onLoad "login-required"})
          (.then #(re-frame/dispatch [::login-finished %])))
      (js/console.log keycloak)
      (assoc db :auth-state {:keycloak keycloak :authenticated false}))))


(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 ::set-active-sidebar-link
 (fn-traced [db [_ active-sidebar-link]]
   (assoc db :active-sidebar-link active-sidebar-link)))

(re-frame/reg-event-db
 ::set-re-pressed-example
 (fn [db [_ value]]
   (assoc db :re-pressed-example value)))

(re-frame/reg-event-db
 ::set-sidebar-expanded
 (fn-traced [db [_ _]]
   (assoc db :sidebar-expanded (not (:sidebar-expanded db)))))

;; Network events, eh.

;;; Fetch Dataflow list
(re-frame/reg-event-fx
  ::fetch-dataflow-list
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc-in [:loading :dataflows] true))
     :http-xhrio {:method            :get
                  :uri               "/dataflow_api/dataflow"
                  :timeout           5000
                  :response-format   (ajax/json-response-format {:keywords? true})
                  :on-success [::fetch-dataflow-list-success]
                  :on-failure [::fetch-dataflow-list-failure]}}))

(re-frame/reg-event-db
  ::fetch-dataflow-list-success
  (fn [db [_ result]]
    (-> db
        (assoc-in [:loading :dataflows] false)
        (assoc :dataflows result))))

(re-frame/reg-event-db
  ::bad-dataflow-list-result
  (fn [db [_ result]]
    (-> db
        (assoc-in [:loading :dataflows] false)
        (assoc :dataflows-error result))))
