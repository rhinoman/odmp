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

(ns odmp-ui.views.collection.collection-modals
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.util.styles :as style]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            [odmp-ui.util.ui :refer [ignore-return]]
            [odmp-ui.components.common :refer [error-alert confirm-dialog]]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/DialogActions" :default DialogActions]))

;; Create Collection Dialog events
(rf/reg-sub
 ::create-collection-dialog-open
 (fn [db _]
   (:create-collection-dialog-open db)))

(rf/reg-event-db
 ::set-collection-create-field
 (fn [db [_ field value]]
   (assoc-in db [:create-collection-dialog-fields field] value)))

(rf/reg-sub
 ::create-collection-name
 (fn [db _]
   (get-in db [:create-collection-dialog-fields :name])))

(rf/reg-sub
 ::create-collection-description
 (fn [db _]
   (get-in db [:create-collection-dialog-fields :description])))

(rf/reg-sub
 ::posting-collection
 (fn [db _]
   (get-in db [:loading :post-collection])))

(rf/reg-sub
 ::posting-collection-errors
 (fn [db _]
   (get-in db [:request-errors :post-collection])))

(rf/reg-sub
 ::delete-collection-dialog-open
 (fn [db _]
   (:delete-collection-dialog-open db)))

(rf/reg-sub
 ::deleting-collection
 (fn [db _]
   (get-in db [:loading :delete-collection])))

(rf/reg-sub
 ::deleting-collection-errors
 (fn [db _]
   (get-in db [:request-errors :delete-collection])))

(rf/reg-sub
 ::delete-dataset-dialog-open
 (fn [db _]
   (:delete-dataset-dialog-open db)))

(rf/reg-sub
 ::deleting-dataset
 (fn [db _]
   (get-in db [:loading :delete-dataset])))

(rf/reg-sub
 ::deleting-dataset-errors
 (fn [db _]
   (get-in db [:request-errors :delete-dataset])))

(rf/reg-event-db
 ::toggle-delete-collection-dialog
 (fn [db [_ _]]
   (-> db
       (assoc :delete-collection-dialog-open
              (not (:delete-collection-dialog-open db))))))

(rf/reg-event-db
 ::toggle-delete-dataset-dialog
 (fn [db [_ _]]
   (-> db
       (assoc :delete-dataset-dialog-open
              (not (:delete-dataset-dialog-open db))))))


(defn modal-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:form-input {"> label" {:color (get-in palette [:success :light])}}}))

;; Save the collection
(defn save-collection [e]
  (let [name-field-value (rf/subscribe [::create-collection-name])
        description-field-value (rf/subscribe [::create-collection-description])]
    (.preventDefault e)
    (rf/dispatch [::events/post-collection {:name @name-field-value
                                            :description @description-field-value}])))

;; DELETE COLLECTION
(defn confirm-delete-collection
  "Shows a confirmation dialog when deleting a collection"
  [collection]
  (let [open (rf/subscribe [::delete-collection-dialog-open])
        is-deleting (rf/subscribe [::deleting-collection])
        errors (rf/subscribe [::deleting-collection-errors])]
    (confirm-dialog open {:question "Confirm Collection Deletion"
                          :text (str "Are you sure you wish to delete the Collection " (:name collection) "?"
                                     " All associated dataset records will also be deleted!")
                          :confirm-action (fn [_]
                                            (rf/dispatch [::events/delete-collection (:id collection)]) 
                                            (rf/dispatch [::toggle-delete-collection-dialog]))
                          :cancel-action #(rf/dispatch [::toggle-delete-collection-dialog])})))

;; DELETE DATASET
(defn confirm-delete-dataset
  "Shows a confirmation dialog when deleting a dataset"
  [dataset collection-id]
  (let [open (rf/subscribe [::delete-dataset-dialog-open])
        is-deleting (rf/subscribe [::deleting-dataset])
        errors (rf/subscribe [::deleting-dataset-errors])]
    (confirm-dialog open {:question "Confirm Dataset Deletion"
                          :text (str "Are you sure you wish to delete the Dataset " (:name dataset) "?")
                          :confirm-action (fn [_]
                                            (rf/dispatch [::events/delete-dataset (:id dataset) collection-id]) 
                                            (rf/dispatch [::toggle-delete-dataset-dialog]))
                          :cancel-action #(rf/dispatch [::toggle-delete-dataset-dialog])})))


(defn create-collection-dialog
  "This is a dialog for creating new Collections"
  []
  (let [open (rf/subscribe [::create-collection-dialog-open])
        is-posting (rf/subscribe [::posting-collection])
        errors (rf/subscribe [::posting-collection-errors])]
    (style/let [classes modal-styles]
      [:> Dialog {:open @open
                  :onClose #(rf/dispatch [::events/toggle-create-collection-dialog])
                  :aria-labelledby "create-collection-dialog-title"}
       [:> DialogTitle {:id "create-collection-dialog-title"} "Create New Collection"]
       [:> DialogContent
        (if @errors (error-alert @errors))
        [:form {:onSubmit save-collection}
         [:> TextField {:autoFocus true
                        :margin :dense
                        :variant :filled
                        :disabled @is-posting
                        :required true
                        :class (:form-input classes)
                        :id :dataflow_name
                        :label "Collection Name"
                        :defaultValue ""
                        :onKeyDown ignore-return
                        :onBlur #(rf/dispatch [::set-collection-create-field :name (-> % .-target .-value)])
                        :type :text
                        :fullWidth true}]
         [:> TextField {:margin :dense
                        :variant :filled
                        :required false
                        :disabled @is-posting
                        :class (:form-input classes)
                        :id :dataflow_description
                        :label "Description"
                        :onBlur #(rf/dispatch [::set-collection-create-field :description (-> % .-target .-value)])
                        :defaultValue ""
                        :onKeyDown ignore-return
                        :type :text
                        :fullWidth true}]
         [:> DialogActions
          [:> Button {:onClick #(rf/dispatch [::events/toggle-create-collection-dialog])} "Cancel"]
          [:> Button {:disabled @is-posting
                      :type :submit :color :primary} "Create"]]]]])))
