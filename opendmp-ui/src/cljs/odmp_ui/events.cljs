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
   [odmp-ui.util.data :as dutil]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [day8.re-frame.forward-events-fx]
   [secretary.core :as secretary]
   ["keycloak-js" :as Keycloak]))

(defn navigate
  "Programmatically navigate to a route"
  [route]
  (set! (.. js/window -location -hash) route))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-fx
 ::auth-complete
 (fn [{:keys [db]} event]
   (secretary/dispatch! (-> js/window .-location .-hash))
   (re-frame/dispatch [::lookup-processor-types])
   (re-frame/dispatch [::lookup-trigger-types])
   {:db db
    :forward-events {:unregister :auth-complete-listener}}))

;; (re-frame/reg-event-fx
;;  ::keycloak-refresh
;;  (fn [{:keys [db]} [_ keycloak]]
;;    {:db db
;;     :http-xhrio {:method :post
;;                  :uri ""}}))

(re-frame/reg-event-fx
 ::keycloak-initialized
 (fn [{:keys [db]} [_ keycloak result]]
   (if (false? result)
     (-> keycloak
         (.login keycloak)
         (.then #(re-frame/dispatch [::keycloak-initialized keycloak %]))
         (.onTokenExpired #(.updateToken keycloak))))
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
            (assoc-in [:loading :dataflow] true))
    :http-xhrio {:method           :get
                 :uri              (str "/dataflow_api/dataflow/" id)
                 :timeout          5000
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :headers (basic-headers db)
                 :on-success [::fetch-dataflow-success]
                 :on-failure [::http-request-failure :dataflow]}}))


;;; Fetch processors for a dataflow
(re-frame/reg-event-fx
 ::fetch-dataflow-processors
 (fn [{:keys [db]} [_ id]]
   {:db (-> db
            (assoc-in [:loading :dataflow] true))
    :http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/dataflow/" id "/processors")
                 :timeout         5000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :headers         (basic-headers db)
                 :on-success      [::fetch-dataflow-processors-success]
                 :on-failure      [::http-request-failure :dataflow-processors]}}))

;;; POST dataflow
(re-frame/reg-event-fx
 ::post-dataflow
 (fn [{:keys [db]} [_ data]]
   {:db (-> db
            (assoc-in [:loading :post-dataflow] true))
    :http-xhrio {:method          :post
                 :uri             "/dataflow_api/dataflow"
                 :params          data
                 :timeout         5000
                 :headers         (basic-headers db)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-post-dataflow]
                 :on-failure      [::http-request-failure :post-dataflow]}}))

;;; DELETE dataflow
(re-frame/reg-event-fx
 ::delete-dataflow
 (fn [{:keys [db]} [_ id]]
   {:db (-> db
            (assoc-in [:loading :delete-dataflow] true))
    :http-xhrio {:method          :delete
                 :uri             (str "/dataflow_api/dataflow/" id)
                 :timeout         5000
                 :headers         (basic-headers db)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-delete-dataflow]
                 :on-failure      [::http-request-failure :delete-dataflow]}}))

;;; POST a Processor
(re-frame/reg-event-fx
 ::post-processor
 (fn [{:keys [db]} [_ data]]
   {:db (-> db
            (assoc-in [:loading :post-processor] true))
    :http-xhrio {:method          :post
                 :uri             (str "/dataflow_api/processor")
                 :timeout         5000
                 :headers         (basic-headers db)
                 :params          data
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-post-processor]
                 :on-failure      [::http-request-failure :post-processor]}}))

;; LOOKUPS
(re-frame/reg-event-fx
 ::lookup-processor-types
 (fn [{:keys [db]} [_]]
   {:http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/lookup/processor_types")
                 :timeout         3000
                 :headers         (basic-headers db)
                 :response-format (ajax/json-response-format)
                 :on-success      [::success-lookup :processor-types]
                 :on-failure      [::http-request-failure :lookup-processor-types]}}))

(re-frame/reg-event-fx
 ::lookup-trigger-types
 (fn [{:keys [db]} [_]]
   {:http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/lookup/trigger_types")
                 :timeout         3000
                 :headers         (basic-headers db)
                 :response-format (ajax/json-response-format)
                 :on-success      [::success-lookup :trigger-types]
                 :on-failure      [::http-request-failure :lookup-trigger-types]}}))

(re-frame/reg-event-db
 ::success-lookup
 (fn [db [_ loc result]]
   (assoc-in db [:lookup loc] result)))

(re-frame/reg-event-db
  ::fetch-dataflow-list-success
  (fn [db [_ result]]
    (-> db
        (assoc-in [:loading :dataflows] false)
        (assoc :dataflows result))))

(re-frame/reg-event-db
 ::fetch-dataflow-success
 (fn [db [_ result]]
   (-> db
       (assoc-in [:loading :dataflow] false)
       (assoc :current-dataflow result))))

(re-frame/reg-event-db
 ::fetch-dataflow-processors-success
 (fn [db [_ result]]
   (-> db
       (assoc-in [:loading :dataflow] false)
       (assoc :current-dataflow-processors result))))

(re-frame/reg-event-db
 ::success-post-dataflow
 (fn [db [_ result]]
   (re-frame/dispatch [::fetch-dataflow-list])
   (re-frame/dispatch [::toggle-create-dataflow-dialog])
   (-> db
       (assoc-in [:loading :post-dataflow] false)
       (assoc-in [:request-errors :post-dataflow] nil))))

(re-frame/reg-event-db
 ::success-delete-dataflow
 (fn [db [_ result]]
   (navigate "/dataflows")
   (-> db
       (assoc-in [:loading :delete-dataflow] false)
       (assoc-in [:request-errors :delete-dataflow] nil))))

(re-frame/reg-event-db
 ::success-post-processor
 (fn [db [_ result]]
   (re-frame/dispatch [::fetch-dataflow-processors (:flowId result)])
   (re-frame/dispatch [::toggle-create-processor-dialog])
   (-> db
       (assoc-in [:loading :post-processor] false)
       (assoc-in [:request-errors :post-processor] nil))))

(re-frame/reg-event-db
  ::http-request-failure
  (fn [db [_ loc result]]
    (let [error-status (get-in result [:parse-error :status])]
      (case error-status
        ;; on 401, try to login again
        401 (do
              (re-frame/dispatch [::keycloak-initialized (get-in db [:auth-state :keycloak]) false])
              (assoc-in db [:loading loc] false))
        ;; on 400, store the errors
        400 (-> db
                (assoc-in [:loading loc] false)
                (assoc-in [:request-errors loc] result))
        ;; else
        (-> db
            (assoc-in [:loading loc] false)
            (assoc-in [:request-errors loc] result))))))

;; Modal events
(re-frame/reg-event-db
 ::toggle-create-dataflow-dialog
 (fn-traced [db [_ _]]
            (-> db
                (assoc :create-dataflow-dialog-fields {})
                (assoc-in [:request-errors :post-dataflow] nil)
                (assoc :create-dataflow-dialog-open (not (:create-dataflow-dialog-open db))))))

(re-frame/reg-event-db
 ::toggle-create-processor-dialog
 (fn-traced [db [_ phase]]
            (let [pnum (or phase 0)]
              (-> db
                  (assoc :create-processor-dialog-fields {})
                  (assoc-in [:request-errors :post-processor] nil)
                  (assoc :create-processor-dialog-open (not (:create-processor-dialog-open db)))
                  (assoc :create-processor-dialog-phase pnum)))))
