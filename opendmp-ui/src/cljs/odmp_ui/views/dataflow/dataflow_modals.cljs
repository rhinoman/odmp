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

(ns odmp-ui.views.dataflow.dataflow-modals
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.util.styles :as style]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            [odmp-ui.components.common :refer [error-alert confirm-dialog]]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/DialogActions" :default DialogActions]))


;; Create Dataflow Dialog events
(rf/reg-sub
 ::create-dataflow-dialog-open
 (fn [db _]
   (:create-dataflow-dialog-open db)))

(rf/reg-event-db
 ::set-dataflow-create-field
 (fn [db [_ field value]]
   (assoc-in db [:create-dataflow-dialog-fields field] value)))

(rf/reg-sub
 ::create-dataflow-name
 (fn [db _]
   (get-in db [:create-dataflow-dialog-fields :name])))

(rf/reg-sub
 ::create-dataflow-description
 (fn [db _]
   (get-in db [:create-dataflow-dialog-fields :description])))

(rf/reg-sub
 ::posting-dataflow
 (fn [db _]
   (get-in db [:loading :post-dataflow])))

(rf/reg-sub
 ::posting-dataflow-errors
 (fn [db _]
   (get-in db [:request-errors :post-dataflow])))

;; Delete Dataflow Dialog events
(rf/reg-sub
 ::delete-dataflow-dialog-open
 (fn [db _]
   (:delete-dataflow-dialog-open db)))

(rf/reg-sub
 ::deleting-dataflow
 (fn [db _]
   (get-in db [:loading :delete-dataflow])))

(rf/reg-sub
 ::deleting-dataflow-errors
 (fn [db _]
   (get-in db [:request-errors :delete-dataflow])))

(rf/reg-event-db
 ::toggle-delete-dataflow-dialog
 (fn [db [_ _]]
   (-> db
       (assoc :delete-dataflow-dialog-open
              (not (:delete-dataflow-dialog-open db))))))

;; styles
(defn modal-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:form-input {"> label" {:color (get-in palette [:success :light])}}}))

;; DELETE DATAFLOW
(defn confirm-delete-dataflow
  "Shows a confirmation dialog when deleting a dataflow"
  [dataflow]
  (let [open (rf/subscribe [::delete-dataflow-dialog-open])
        is-deleting (rf/subscribe [::deleting-dataflow])
        errors (rf/subscribe [::deleting-dataflow-errors])]
    (confirm-dialog open {:question "Confirm Dataflow Deletion"
                          :text (str "Are you sure you wish to delete the Dataflow " (:name dataflow) "?"
                                     " All associated processors will also be deleted!")
                          :confirm-action (fn [_]
                                            (rf/dispatch [::events/delete-dataflow (:id dataflow)]) 
                                            (rf/dispatch [::toggle-delete-dataflow-dialog]))
                          :cancel-action #(rf/dispatch [::toggle-delete-dataflow-dialog])})))

;; SAVE DATAFLOW
(defn save-dataflow [e]
  (let [name-field-value (rf/subscribe [::create-dataflow-name])
        description-field-value (rf/subscribe [::create-dataflow-description])]
    (.preventDefault e)
    (rf/dispatch [::events/post-dataflow {:name @name-field-value
                                          :description @description-field-value}])))

(defn create-dataflow-dialog
  "This is a dialog for creating new Dataflows"
  []
  (let [open (rf/subscribe [::create-dataflow-dialog-open])
        name-field-value (rf/subscribe [::create-dataflow-name])
        description-field-value (rf/subscribe [::create-dataflow-description])
        is-posting (rf/subscribe [::posting-dataflow])
        errors (rf/subscribe [::posting-dataflow-errors])]
    (style/let [classes modal-styles]
      [:> Dialog {:open @open
                  :onClose #(rf/dispatch [::events/toggle-create-dataflow-dialog])
                  :aria-labelledby "create-dataflow-dialog"}
       [:> DialogTitle "Create New Dataflow"]
       [:> DialogContent
        (if @errors (error-alert @errors))
        [:form {:onSubmit save-dataflow}
         [:> TextField {:autoFocus true
                        :margin :dense
                        :variant :filled
                        :disabled @is-posting
                        :required true
                        :class (:form-input classes)
                        :id :dataflow_name
                        :label "Dataflow Name"
                        :value (or @name-field-value "")
                        :onChange #(rf/dispatch [::set-dataflow-create-field :name (-> % .-target .-value)])
                        :type :text
                        :fullWidth true}]
         [:> TextField {:margin :dense
                        :variant :filled
                        :required false
                        :disabled @is-posting
                        :class (:form-input classes)
                        :id :dataflow_description
                        :label "Description"
                        :onChange #(rf/dispatch [::set-dataflow-create-field :description (-> % .-target .-value)])
                        :value (or @description-field-value "")
                        :type :text
                        :fullWidth true}]
         [:> DialogActions
          [:> Button {:onClick #(rf/dispatch [::events/toggle-create-dataflow-dialog])} "Cancel"]
          [:> Button {:disabled @is-posting
                      :type :submit :color :primary} "Create"]]]]])))
