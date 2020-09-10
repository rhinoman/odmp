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

(ns odmp-ui.views.processor.events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 ::clear-processor-edit-fields
 (fn [db [_ _]]
   (assoc db :edit-processor-fields {})))

(rf/reg-event-db
 ::set-processor-edit-field
 (fn [db [_ field value]]
   (assoc-in db [:edit-processor-fields field] value)))

(rf/reg-event-db
 ::set-processor-property
 (fn [db [_ field value]]
   (assoc-in db [:edit-processor-fields :properties field] value)))

(rf/reg-event-db
 ::set-processor-input-type-field
 (fn [db [_ idx value]]
   (assoc-in db [:edit-processor-fields :inputs idx :sourceType] value)))

(rf/reg-event-db
 ::set-processor-input-location-field
 (fn [db [_ idx value]]
   (assoc-in db [:edit-processor-fields :inputs idx :sourceLocation] value)))

(rf/reg-event-db
 ::set-processor-input-additional-property
 (fn [db [_ idx property value]]
   (assoc-in db [:edit-processor-fields :inputs idx :additionalProperties property] value)))

(rf/reg-event-db
 ::toggle-delete-processor-dialog
 (fn [db [_ _]]
   (-> db
       (assoc :delete-processor-dialog-open
              (not (:delete-processor-dialog-open db))))))
