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

(ns odmp-ui.events
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [odmp-ui.db :as db]
   [odmp-ui.util.data :as dutil]
   [ajax.core :as ajax]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [day8.re-frame.forward-events-fx]
   [secretary.core :as secretary]
   [cljs.core.async :refer [<! timeout]]
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
 ::fetch-user-info
 (fn [{:keys [db]}]
   (-> (get-in db [:auth-state :keycloak])
       (.loadUserInfo)
       (.then #(re-frame/dispatch [::set-user-info %])))
   {:db db}))

(re-frame/reg-event-db
 ::set-user-info
 (fn [db [_ info]]
   (assoc-in db [:user :info] (js->clj info :keywordize-keys true))))

(re-frame/reg-event-fx
 ::auth-refresh
 (fn [{:keys [db]}]
   (let [kc (get-in db [:auth-state :keycloak])]
     (go
       (<! (timeout 30000))
       (.updateToken kc 30)
       (re-frame/dispatch [::auth-refresh]))
     {:db db})))

(re-frame/reg-event-fx
 ::auth-complete
 (fn [{:keys [db]} event]
   (re-frame/dispatch [::lookup-processor-types])
   (re-frame/dispatch [::lookup-trigger-types])
   (re-frame/dispatch [::fetch-user-info])
   (re-frame/dispatch [::auth-refresh])
   (secretary/dispatch! (-> js/window .-location .-hash))
   {:db db
    :forward-events {:unregister :auth-complete-listener}}))

(re-frame/reg-event-fx
 ::keycloak-initialized
 (fn [{:keys [db]} [_ ^js keycloak result]]
   (if (false? result)
     (-> keycloak
         (.login)
         (.then #(re-frame/dispatch [::keycloak-initialized keycloak %]))))
     {:db (assoc db :auth-state {:keycloak keycloak :authenticated result})}))

(re-frame/reg-event-fx
 ::initialize-keycloak
  (fn [{:keys [db]} [_ _]]
    (let [^js keycloak (Keycloak "/assets/keycloak.json")]
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

(re-frame/reg-event-fx
 ::logout
 (fn [{:keys [db]}]
   (let [^js keycloak (get-in db [:auth-state :keycloak])]
     (.logout keycloak))))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel id]]
            (assoc db :active-panel {:panel active-panel :resource-id id})))

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


(re-frame/reg-event-db
 ::clear-collection-list
 (fn [db [_ _]]
   (assoc db :collection nil)))

;;; Get a refresh token from keycloak
(re-frame/reg-event-fx
 ::refresh-token
 (fn [{:keys [db]} _]
   (let [^js kc (get-in db [:auth-state :keycloak])
         rtok (. kc -refreshToken)
         client-id (. kc -clientId)
         params {:grant_type "refresh_token" :refresh_token rtok :client_id client-id}
         uri (.. kc -endpoints -token)]
    {:http-xhrio {:method          :post
                  :uri             (uri)
                  :timeout         2000
                  :format          (ajax/url-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :headers         (basic-headers db)
                  :params          params
                  :on-success      [::refresh-token-success]
                  :on-failure      [::initialize-keycloak]}})))

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
                 :headers          (basic-headers db)
                 :on-success       [::fetch-dataflow-success]
                 :on-failure       [::http-request-failure :dataflow]}}))

;;; Fetch a dataflow run status
(re-frame/reg-event-fx
 ::fetch-dataflow-runplan-status
 (fn [{:keys [db]} [_ id]]
   {:db db
    :http-xhrio {:method           :get
                 :uri              (str "/dataflow_api/dataflow/" id "/run_plan_status")
                 :timeout          5000
                 :response-format  (ajax/json-response-format {:keywords? true})
                 :headers          (basic-headers db)
                 :on-success       [::fetch-dataflow-runplan-status-success]
                 :on-failure       [::http-request-failure :dataflow-runplan-status]}}))


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

;;; UPDATE dataflow
(re-frame/reg-event-fx
 ::update-dataflow
 (fn [{:keys [db]} [_ id flow]]
   {:db (-> db
            (assoc-in [:loading :put-dataflow] true))
    :http-xhrio {:method          :put
                 :uri             (str "/dataflow_api/dataflow/" id)
                 :timeout         5000
                 :headers         (basic-headers db)
                 :params          flow
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-put-dataflow]
                 :on-failure      [::http-request-failure :put-dataflow]}}))

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

;;; FETCH a processor
(re-frame/reg-event-fx
 ::fetch-processor
 (fn [{:keys [db]} [_ id opts]]
   {:db (-> db
            (assoc-in [:loading :processor] true))
    :http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/processor/" id)
                 :timeout         5000
                 :headers         (basic-headers db)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-fetch-processor opts]
                 :on-failure      [::http-request-failure :get-processor]}}))

;;; UPDATE a processor
(re-frame/reg-event-fx
 ::update-processor
 (fn [{:keys [db]} [_ id proc]]
   {:db (-> db
            (assoc-in [:loading :put-processor] true))
    :http-xhrio {:method          :put
                 :uri             (str "/dataflow_api/processor/" id)
                 :timeout         5000
                 :headers         (basic-headers db)
                 :params          proc
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-put-processor]
                 :on-failure      [::http-request-failure :put-processor]}}))

;;; DELETE a processor
(re-frame/reg-event-fx
 ::delete-processor
 (fn [{:keys [db]} [_ id parent-id]]
   {:db (-> db
            (assoc-in [:loading :delete-processor] true))
    :http-xhrio {:method          :delete
                 :uri             (str "/dataflow_api/processor/" id)
                 :timeout         5000
                 :headers         (basic-headers db)
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::success-delete-processor parent-id]
                 :on-failure      [::http-request-failure :delete-processor]}}))

;;; LIST Collections
(re-frame/reg-event-fx
  ::fetch-collection-list
  (fn [{:keys [db]} _]
    {:http-xhrio {:method            :get
                  :uri               "/dataflow_api/collection"
                  :timeout           5000
                  :response-format   (ajax/json-response-format {:keywords? true})
                  :headers           (basic-headers db)
                  :on-success        [::fetch-collection-list-success]
                  :on-failure        [::http-request-failure :collections]}}))

;;; Fetch an individual collection
(re-frame/reg-event-fx
 ::fetch-collection
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio {:method             :get
                 :uri                (str "/dataflow_api/collection/" id)
                 :timeout            5000
                 :response-format    (ajax/json-response-format {:keywords? true})
                 :headers            (basic-headers db)
                 :on-success         [::fetch-collection-success]
                 :on-failure         [::http-request-failure :get-collection]}}))

;;; POST a new collection
(re-frame/reg-event-fx
 ::post-collection
 (fn [{:keys [db]} [_ data]]
   {:db (-> db
            (assoc-in [:loading :post-collection] true))
    :http-xhrio {:method             :post
                 :uri                "/dataflow_api/collection"
                 :params             data
                 :timeout            5000
                 :headers            (basic-headers db)
                 :format             (ajax/json-request-format)
                 :response-format    (ajax/json-response-format {:keywords? true})
                 :on-success         [::success-post-collection]
                 :on-failure         [::http-request-failure :post-collection]}}))

;;; GET a collection's datasets
(re-frame/reg-event-fx
 ::fetch-collection-datasets
 (fn [{:keys [db]} [_ id query-params]]
   {:http-xhrio {:method             :get
                 :uri                (str "/dataflow_api/collection/" id "/datasets")
                 :timeout            5000
                 :response-format    (ajax/json-response-format {:keywords? true})
                 :headers            (basic-headers db)
                 :params             query-params
                 :on-success         [::fetch-collection-datasets-success]
                 :on-failure         [::http-request-failure :collection-datasets]}}))

;;; GET a count of a collection's dataset
(re-frame/reg-event-fx
 ::fetch-collection-dataset-count
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio {:method             :get
                 :uri                (str "/dataflow_api/collection/" id "/datasets/count")
                 :timeout            5000
                 :response-format    (ajax/json-response-format {:keywords? true})
                 :headers            (basic-headers db)
                 :on-success         [::fetch-dataset-count-success]
                 :on-failure         [::http-request-failure :collection-dataset-count]}}))

;; REQUEST a dataset download
(re-frame/reg-event-fx
 ::request-dataset-download
 (fn [{:keys [db]} [_ id]]
   {:http-xhrio {:method             :get
                 :uri                (str "/dataflow_api/dataset/" id "/request_download")
                 :timeout            5000
                 :response-format    (ajax/json-response-format {:keywords? true})
                 :headers            (basic-headers db)
                 :on-success         [::request-dataset-download-success]
                 :on-failure         [::http-request-failure ::request-dataset-download]}}))

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

(re-frame/reg-event-fx
 ::lookup-source-types
 (fn [{:keys [db]} [_ ptype]]
   {:http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/lookup/source_types?processorType=" ptype)
                 :timeout         3000
                 :headers         (basic-headers db)
                 :response-format (ajax/json-response-format)
                 :on-success      [::success-lookup :source-types]
                 :on-failure      [::http-request-failure :lookup-source-types]}}))

(re-frame/reg-event-fx
 ::lookup-destination-types
 (fn [{:keys [db]} [_ ptype]]
   {:http-xhrio {:method          :get
                 :uri             (str "/dataflow_api/lookup/destination_types")
                 :timeout         3000
                 :headers         (basic-headers db)
                 :response-format (ajax/json-response-format)
                 :on-success      [::success-lookup :destination-types]
                 :on-failure      [::http-request-failure :lookup-destination-types]}}))

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
 ::fetch-dataflow-runplan-status-success
 (fn [db [_ result]]
   (-> db
       (assoc :current-dataflow-runplan-status result))))

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
   (re-frame/dispatch [::set-snackbar "success" "Dataflow Created"])
   (-> db
       (assoc-in [:loading :post-dataflow] false)
       (assoc-in [:request-errors :post-dataflow] nil))))

(re-frame/reg-event-db
 ::success-put-dataflow
 (fn [db [_ result]]
   (re-frame/dispatch [::set-snackbar "success" "Dataflow Saved"])
   (-> db
       (assoc-in [:loading :put-dataflow] false)
       (assoc-in [:request-errors :put-dataflow] nil)
       (assoc :current-dataflow result))))

(re-frame/reg-event-db
 ::success-delete-dataflow
 (fn [db [_ result]]
   (navigate "/dataflows")
   (re-frame/dispatch [::set-snackbar "success" "Dataflow Deleted"])
   (-> db
       (assoc-in [:loading :delete-dataflow] false)
       (assoc-in [:request-errors :delete-dataflow] nil))))

(re-frame/reg-event-db
 ::success-post-processor
 (fn [db [_ result]]
   (re-frame/dispatch [::fetch-dataflow-processors (:flowId result)])
   (re-frame/dispatch [::toggle-create-processor-dialog])
   (re-frame/dispatch [::set-snackbar "success" "Processor Created"])
   (-> db
       (assoc-in [:loading :post-processor] false)
       (assoc-in [:request-errors :post-processor] nil))))

(re-frame/reg-event-db
 ::success-fetch-processor
 (fn [db [_ opts result]]
   (re-frame/dispatch [::lookup-source-types (get-in result [:processor :type])])
   (if (:load-processors? opts)
     (re-frame/dispatch [::fetch-dataflow-processors (get-in result [:processor :flowId])]))
   (if (:load-runplan-status? opts)
     (re-frame/dispatch [::fetch-dataflow-runplan-status (get-in result [:processor :flowId])]))
   (-> db
       (assoc-in [:loading :processor] false)
       (assoc :current-processor (:processor result))
       (assoc :current-dataflow (:dataflow result)))))

(re-frame/reg-event-db
 ::success-put-processor
 (fn [db [_ result]]
   (re-frame/dispatch [::set-snackbar "success" "Processor Saved"])
   (-> db
       (assoc-in [:loading :put-processor] false)
       (assoc-in [:request-errors :put-processor] nil)
       (assoc :current-processor result))))

(re-frame/reg-event-db
 ::success-delete-processor
 (fn [db [_ parent-id result]]
   (navigate (str "/dataflows/" parent-id))
   (re-frame/dispatch [::set-snackbar "success" "Processor Deleted"])
   (-> db
       (assoc-in [:loading :delete-processor] false)
       (assoc-in [:request-errors :delete-processor] nil))))

(re-frame/reg-event-db
 ::fetch-collection-list-success
 (fn [db [_ result]]
   (-> db
       (assoc :collections result))))

(re-frame/reg-event-db
 ::fetch-collection-success
 (fn [db [_ result]]
   (-> db
       (assoc-in [:loading :collection] false)
       (assoc :current-collection result))))

(re-frame/reg-event-db
 ::success-post-collection
 (fn [db [_ result]]
   (re-frame/dispatch [::fetch-collection-list])
   (re-frame/dispatch [::toggle-create-collection-dialog])
   (re-frame/dispatch [::set-snackbar "success" "Collection Created"])
   (-> db
       (assoc-in [:loading :post-collection] false)
       (assoc-in [:request-errors :post-collection] nil))))

(re-frame/reg-event-db
 ::fetch-collection-datasets-success
 (fn [db [_ result]]
   (-> db
       (assoc :current-collection-datasets result))))

(re-frame/reg-event-db
 ::fetch-dataset-count-success
 (fn [db [_ result]]
   (-> db
       (assoc :current-collection-dataset-count (:totalCount result)))))

(re-frame/reg-event-fx
 ::request-dataset-download-success
 (fn [{:keys [db]} [_ result]]
   (let [token (:token result)
         file-url (str "/dataflow_api/dataset/download?token=" token)
         iframe (.. js/document (getElementById "downloaderIframe"))]
     (println iframe)
     (set! (-> iframe .-src) file-url)
     )
   {:db db}))

(re-frame/reg-event-db
  ::http-request-failure
  (fn [db [_ loc result]]
    (let [error-status (or (get result :status)
                           (get-in result [:parse-error :status]))]
      (cond
        ;; on 401, try to login again
        (= error-status 401) (do
              (re-frame/dispatch [::keycloak-initialized (get-in db [:auth-state :keycloak]) false])
              (assoc-in db [:loading loc] false))
        ;; on 400, store the errors
        (some #{error-status} [400 409])
          (do
            (re-frame/dispatch [::set-snackbar "error" (or (get-in result [:response :message]) "Bad Request") ])
            (-> db
                (assoc-in [:loading loc] false)
                (assoc-in [:request-errors loc] result)))
        :else (do
          (-> db
              (assoc-in [:loading loc] false)
              (assoc-in [:request-errors loc] result)))))))

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

(re-frame/reg-event-db
 ::toggle-create-collection-dialog
 (fn-traced [db [_ _]]
            (-> db
                (assoc :create-collection-dialog-fields {})
                (assoc-in [:request-errors :post-collection] nil)
                (assoc :create-collection-dialog-open (not (:create-collection-dialog-open db))))))

(re-frame/reg-event-db
 ::set-snackbar
 (fn-traced [db [_ severity text]]
   (assoc db :snackbar {:open true :severity severity :text text})))

(re-frame/reg-event-db
 ::clear-snackbar
 (fn-traced [db [_ _]]
   (assoc db :snackbar nil)))

(re-frame/reg-event-db
 ::clear-collection-data
 (fn [db [_ _]]
   (-> db
       (assoc :current-collection nil)
       (assoc :collections nil)
       (assoc :current-collection-datasets nil)
       (assoc :current-collection-dataset-count nil))))

(re-frame/reg-event-db
 ::clear-dataflow-data
 (fn [db [_ _]]
   (-> db
       (assoc :current-dataflow nil)
       (assoc :dataflows nil)
       (assoc :current-dataflow-processors nil)
       (assoc :current-dataflow-runplan-status nil))))

(re-frame/reg-event-db
 ::clear-processor-data
 (fn [db [_ _]]
   (-> db
       (assoc :current-processor nil))))
