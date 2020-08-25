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

(ns odmp-ui.subs
  (:require
   [re-frame.core :as re-frame]
   [re-pressed.core :as rp]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::active-sidebar-link
 (fn [db _]
   (:active-sidebar-link db)))

(re-frame/reg-sub
 ::sidebar-expanded
 (fn [db _]
   (:sidebar-expanded db)))

(re-frame/reg-sub
 ::dark-theme?
 (fn [db _]
   (:dark-theme? db)))

(re-frame/reg-sub
 ::keydown-keys
 (fn [db _]
   (get-in db [:re-pressed.core/keydown :keys])))

(re-frame/reg-sub
 ::authentication
  (fn [db _]
    (:auth-state db)))

;; The list of all dataflows
(re-frame/reg-sub
 ::dataflows
 (fn [db _]
   (:dataflows db)))

;; The currently loaded/displayed dataflow
(re-frame/reg-sub
 ::current-dataflow
 (fn [db _]
   (:current-dataflow db)))

;; The runplan for the current dataflow
(re-frame/reg-sub
 ::current-dataflow-runplan-status
 (fn [db _]
   (:current-dataflow-runplan-status db)))

;; Errors for the current runplan status
(re-frame/reg-sub
 ::current-dataflow-processor-errors
 (fn [db _]
   (get-in db [:current-dataflow-runplan-status :processorErrors])))

;; List of processors currently being examined
(re-frame/reg-sub
 ::current-dataflow-processors
 (fn [db _]
   (:current-dataflow-processors db)))

;; Loading status of dataflow
(re-frame/reg-sub
 ::loading-dataflows
 (fn [db _]
   (get-in db [:loading :dataflow])))

;; The currently loaded/displayed processor for editing
(re-frame/reg-sub
 ::current-processor
 (fn [db _]
   (:current-processor db)))

;; The list of all collections
(re-frame/reg-sub
 ::collections
 (fn [db _]
   (:collections db)))

(re-frame/reg-sub
 ::current-collection
 (fn [db _]
   (:current-collection db)))

(re-frame/reg-sub
 ::current-collection-datasets
 (fn [db _]
   (:current-collection-datasets db)))

;; Lookups
(re-frame/reg-sub
 ::lookup-processor-types
 (fn [db _]
   (get-in db [:lookup :processor-types])))

(re-frame/reg-sub
 ::lookup-trigger-types
 (fn [db _]
   (get-in db [:lookup :trigger-types])))

(re-frame/reg-sub
 ::lookup-source-types
 (fn [db _]
   (get-in db [:lookup :source-types])))

(re-frame/reg-sub
 ::lookup-destination-types
 (fn [db _]
   (get-in db [:lookup :destination-types])))

;; User
(re-frame/reg-sub
 ::user-info
 (fn [db _]
   (get-in db [:user :info])))

;; snackbar for status updates
(re-frame/reg-sub
 ::snackbar
 (fn [db _]
   (:snackbar db)))

(re-frame/reg-sub
 ::errors
 (fn [db _]
   (:errors db)))
