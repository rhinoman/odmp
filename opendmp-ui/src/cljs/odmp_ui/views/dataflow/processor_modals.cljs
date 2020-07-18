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

(ns odmp-ui.views.dataflow.processor-modals
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.util.styles :as style]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            [odmp-ui.components.common :refer [error-alert confirm-dialog]]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/FormControl" :default FormControl]
            ["@material-ui/core/InputLabel" :default InputLabel]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/DialogActions" :default DialogActions]))


(rf/reg-event-db
 ::set-processor-create-field
 (fn [db [_ field value]]
   (assoc-in db [:create-processor-dialog-fields field] value)))

(rf/reg-sub
 ::create-processor-dialog-open
 (fn [db _]
   (:create-processor-dialog-open db)))

(rf/reg-sub
 ::posting-processor-errors
 (fn [db _]
   (get-in db [:request-errors :post-processor])))

(rf/reg-sub
 ::posting-processor
 (fn [db _]
   (get-in db [:loading :post-processor])))

(rf/reg-sub
 ::phase-from-flow
 (fn [db _]
   (get-in db [:create-processor-dialog-phase])))

(rf/reg-sub
 ::create-processor-name
 (fn [db _]
   (get-in db [:create-processor-dialog-fields :name])))

(rf/reg-sub
 ::create-processor-description
 (fn [db _]
   (get-in db [:create-processor-dialog-fields :description])))

(rf/reg-sub
 ::phase-override
 (fn [db _]
   (get-in db [:create-processor-dialog-fields :phase])))

(rf/reg-sub
 ::create-processor-type
 (fn [db _]
   (get-in db [:create-processor-dialog-fields :processor-type])))

;; styles
(defn modal-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:form-input {"> label" {:color (get-in palette [:success :light])}}}))

;; SAVE PROCESSOR
(defn save-processor [e flowId]
  (let [phase-input (rf/subscribe [::phase-from-flow])
        phase-override (rf/subscribe [::phase-override])
        name-field-value (rf/subscribe [::create-processor-name])
        description-field-value (rf/subscribe [::create-processor-description])
        proc-type-field-value (rf/subscribe [::create-processor-type])]
    (.preventDefault e)
    (rf/dispatch [::events/post-processor {:name @name-field-value
                                           :description @description-field-value
                                           :phase (or @phase-override @phase-input)
                                           :type @proc-type-field-value
                                           :flowId flowId}])))

(defn create-processor-modal
  "This is a dialog for creating new Processors"
  [flowId]
  (let [open (rf/subscribe [::create-processor-dialog-open])
        is-posting (rf/subscribe [::posting-processor])
        errors (rf/subscribe [::posting-processor-errors])
        proc-types (rf/subscribe [::subs/lookup-processor-types])
        phase-input (rf/subscribe [::phase-from-flow])
        phase-override (rf/subscribe [::phase-override])
        name-field-value (rf/subscribe [::create-processor-name])
        description-field-value (rf/subscribe [::create-processor-description])
        proc-type-field-value (rf/subscribe [::create-processor-type])]
    (style/let [classes modal-styles]
      [:> Dialog {:open @open
                  :onClose #(rf/dispatch [::events/toggle-create-processor-dialog])
                  :aria-labelledby "create-processor-dialog-title"}
       [:> DialogTitle {:id "create-processor-dialog-title"} "Create New Processor"]
       [:> DialogContent
        (if @errors (error-alert @errors))
        [:form {:onSubmit #(save-processor % flowId)}
         [:> TextField {:autoFocus true
                        :margin :dense
                        :variant :filled
                        :disabled @is-posting
                        :required true
                        :class (:form-input classes)
                        :id :processor_name
                        :label "Processor Name"
                        :value (or @name-field-value "")
                        :onChange #(rf/dispatch [::set-processor-create-field :name (-> % .-target .-value)])
                        :type :text
                        :fullWidth true}]
         [:> TextField {:margin :dense
                        :variant :filled
                        :required false
                        :disabled @is-posting
                        :class (:form-input classes)
                        :id :processor_description
                        :label "Description"
                        :onChange #(rf/dispatch [::set-processor-create-field :description (-> % .-target .-value)])
                        :value (or @description-field-value "")
                        :type :text
                        :fullWidth true}]
         [:> Grid {:container true :spacing 2}
          [:> Grid {:item true :xs 3}
           [:> TextField {:margin :dense
                          :variant :filled
                          :required true
                          :disabled @is-posting
                          :class (:form-input classes)
                          :id :processor_phase
                          :label "Phase"
                          :value (or @phase-override @phase-input)
                          :type :number
                          :onChange #(rf/dispatch [::set-processor-create-field :phase (-> % .-target .-value)])}]]
          [:> Grid {:item true :xs 9}
           [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
            [:> InputLabel {:id "select-processor-type-label"} "Processor Type"]
            [:> Select {:labelid "select-processor-type-label"
                        :value (or @proc-type-field-value "")
                        :onChange #(rf/dispatch [::set-processor-create-field :processor-type (-> % .-target .-value)])}
             [:> MenuItem {:value ""} [:em "None"]]
             (map (fn [pt] ^{:key (str "MI_" pt)} [:> MenuItem {:value pt} pt]) @proc-types)]]]]
         [:> DialogActions
          [:> Button {:onClick #(rf/dispatch [::events/toggle-create-processor-dialog])} "Cancel"]
          [:> Button {:disabled @is-posting
                      :type :submit
                      :color :primary} "Create"]]]]])))
