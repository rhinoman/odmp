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

(ns odmp-ui.views.processor.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::updating-processor-errors
 (fn [db _]
   (get-in db [:request-errors :put-processor])))

(rf/reg-sub
 ::updating-processor
 (fn [db _]
   (get-in db [:loading :put-processor])))

(rf/reg-sub
 ::delete-processor-dialog-open
 (fn [db _]
   (:delete-processor-dialog-open db)))

(rf/reg-sub
 ::deleting-processor
 (fn [db _]
   (get-in db [:loading :delete-processor])))

(rf/reg-sub
 ::deleting-processor-errors
 (fn [db _]
   (get-in db [:request-errors :delete-processor])))

;;Editor fields
(rf/reg-sub
 ::edit-processor-type
 (fn [db _]
   (get-in db [:edit-processor-fields :processor-type])))

(rf/reg-sub
 ::edit-inputs
 (fn [db _]
   (get-in db [:edit-processor-fields :inputs])))

(rf/reg-sub
 ::edit-properties
 (fn [db _]
   (get-in db [:edit-processor-fields :properties])))

(rf/reg-sub
 ::edit-script-language
 (fn [db _]
   (get-in db [:edit-processor-fields :properties :language])))
