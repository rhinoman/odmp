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
   [day8.re-frame.forward-events-fx]
   [secretary.core :as secretary]
   ["keycloak-js" :as Keycloak]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))


(re-frame/reg-event-fx
 ::auth-complete
 (fn [{:keys [db]} event]
   (secretary/dispatch! (-> js/window .-location .-hash))
   {:db db
    :forward-events {:unregister :auth-complete-listener}}))

(re-frame/reg-event-fx
 ::keycloak-initialized
 (fn [{:keys [db]} [_ keycloak result]]
   (if (false? result)
     (-> keycloak
         (.login keycloak)
         (.then #(re-frame/dispatch [::keycloak-initialized keycloak %]))))
     {:db (assoc db :auth-state {:keycloak keycloak :authenticated result})}))

(re-frame/reg-event-db
 ::refresh-keycloak
 (fn [db [_ _]]
   (let [keycloak (get-in db [:auth-state :keycloak])]
     (println "Refreshing token")
     (-> keycloak
         (.updateToken 30)
         (.success #(println "got a token"))))))

(re-frame/reg-event-fx
 ::initialize-keycloak
  (fn [{:keys [db]} [_ _]]
    (let [keycloak (Keycloak "/assets/keycloak.json")]
      ;(set! (.-onTokenExpired keycloak) #(re-frame/dispatch [::refresh-keycloak]))
      (-> keycloak
          (.init #js{:onLoad "check-sso"
                     :checkLoginIframe false
                     :silentCheckSsoRedirectUri (str (-> js/window .-location .-origin) "/silent-check-sso.html")})
          (.then #(re-frame/dispatch [::keycloak-initialized keycloak %]))
          (.catch #(js/console.error %)))
      {:db (assoc db :auth-state {:keycloak keycloak :authenticated false})
       :forward-events {:register :auth-complete-listener
                        :events #{::keycloak-initialized}
                        :dispatch-to [::auth-complete]}})))


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

(defn get-token [db]
  (let [keycloak (get-in db [:auth-state :keycloak])]
    (.-token keycloak)))

(defn basic-headers [db]
  {:Authorization (str "Bearer " (get-token db))})

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
                  :headers (basic-headers db)
                  :on-success [::fetch-dataflow-list-success]
                  :on-failure [::http-request-failure :dataflows]}}))

;;; Fetch individual dataflow
(re-frame/reg-event-fx
 ::fetch-dataflow
 (fn [{:keys [db]} [_ id]]
   {:db (-> db
            (assoc-in [:loading :dataflows] true))
    :http-xhrio {:method           :get
                 :uri              (str "/dataflow_api/dataflow/" id)
                 :timeout          5000
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :headers (basic-headers db)}}))

(re-frame/reg-event-db
  ::fetch-dataflow-list-success
  (fn [db [_ result]]
    (-> db
        (assoc-in [:loading :dataflows] false)
        (assoc :dataflows result))))

(re-frame/reg-event-db
  ::http-request-failure
  (fn [db [_ loc result]]
    (let [error-status (get-in result [:parse-error :status])]
      (case error-status
        ;; on 401, try to login again
        401 (re-frame/dispatch [::keycloak-initialized (get-in db [:auth-state :keycloak]) false])
        :else (-> db
                  (js/console.error result)
                  (assoc-in [:loading loc] false)
                  (assoc-in [:errors loc] result))))))
