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

(ns odmp-ui.views.processor.edit
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.events :as events]
            [odmp-ui.views.processor.events :as proc-events]
            [odmp-ui.views.processor.subs :as proc-subs]
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.network :as net]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.components.icons :refer [processor-type-icon]]
            [odmp-ui.views.processor.input-fields :refer [input-fields]]
            [odmp-ui.views.processor.script-fields :refer [script-fields]]
            [odmp-ui.views.processor.collect-fields :refer [collect-fields]]
            [odmp-ui.views.processor.external-fields :refer [external-fields]]
            [odmp-ui.views.processor.styles :refer [proc-styles]]
            [clojure.string :as str]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/lab/Alert" :default Alert]
            ["@material-ui/core/Container" :default Container]
            ["@material-ui/core/Paper" :default Card]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Tooltip" :default Tooltip]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/FormControl" :default FormControl]
            ["@material-ui/core/InputLabel" :default InputLabel]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/icons/DeleteTwoTone" :default DeleteIcon]
            ["@material-ui/icons/SaveTwoTone" :default SaveIcon]))


;; DELETE PROCESSOR
(defn confirm-delete-processor
  "Shows a confrmation dialog when delete a processor"
  [processor]
  (let [open (rf/subscribe [::proc-subs/delete-processor-dialog-open])
        is-deleting (rf/subscribe [::proc-subs/deleting-processor])
        errors (rf/subscribe [::proc-subs/deleting-processor-errors])]
    (tcom/confirm-dialog open {:question "Confirm Processor Deletion"
                               :text (str "Are you sure you wish to delete the Processor " (:name processor) "?")
                               :confirm-action (fn [_]
                                                 (rf/dispatch [::events/delete-processor
                                                               (:id processor)
                                                               (:flowId processor)])
                                                 (rf/dispatch [::proc-events/toggle-delete-processor-dialog]))
                               :cancel-action #(rf/dispatch [::proc-events/toggle-delete-processor-dialog])})))


(defn update-input [cur-inputs field-inputs]
  (reduce-kv (fn [m k v]
               (let [st (:sourceType v)
                     sl (:sourceLocation v)
                     sp (:additionalProperties v)
                     curt (get-in cur-inputs [k :sourceType])
                     curl (get-in cur-inputs [k :sourceLocation])
                     curp (get-in cur-inputs [k :additionalProperties])]
                 (-> m
                     (assoc-in [k :sourceType] (or st curt))
                     (assoc-in [k :sourceLocation] (or sl curl))
                     (assoc-in [k :additionalProperties] (or sp curp)))))
             (or cur-inputs []) field-inputs))
   

(defn update-properties [cur-props field-props]
  (reduce-kv (fn [m k v]
               (assoc m k v))
             (or cur-props []) field-props))

(defn save-processor
  "Updates the processor"
  [evt processor]
  (.preventDefault evt)
  (let [name-field (rf/subscribe [::proc-subs/edit-name])
        description-field (rf/subscribe [::proc-subs/edit-description])
        input-fields (rf/subscribe [::proc-subs/edit-inputs])
        properties (rf/subscribe [::proc-subs/edit-properties])
        up-name (or @name-field (:name processor))
        up-description (or @description-field (:description processor))
        up-inputs (update-input (:inputs processor) @input-fields)
        up-props (update-properties (:properties processor) @properties)
        updated-processor (-> processor
                              (assoc :name up-name)
                              (assoc :description up-description)
                              (assoc :inputs up-inputs)
                              (assoc :properties up-props))]
    (rf/dispatch [::events/update-processor (:id processor) updated-processor])))

(defn common-fields
  "Displays fields common to all processors"
  [processor flow-enabled]
  (let []
    (style/let [classes proc-styles]
      [:> CardHeader {:avatar (r/as-element (processor-type-icon (:type @processor)))
                      :title (str (str/capitalize (:type @processor)) " Processor")
                      :titleTypographyProps {:variant :h6 :gutterBottom true}
                      ;:subheader "Cool stuff"
                      :action (r/as-element [:> Button {:size :medium
                                                        :type :submit
                                                        :class (:save-action-button classes)
                                                        :color :primary
                                                        
                                                        :disabled flow-enabled
                                                        :disableElevation true
                                                        :variant :contained
                                                        :startIcon (r/as-element [:> SaveIcon])} "Save"])}])))

(defn show-processor-errors
  [errors]
  [:> Alert {:severity :error}
   "This processor has experienced errors"
   [:ul
    (map (fn [err] ^{:key (:id err)} [:li (:errorMessage err)]) errors)]])

(defn processor-editor*
  "Main Component for editing processors"
  []
  (let [processor (rf/subscribe [::subs/current-processor])
        dataflow  (rf/subscribe [::subs/current-dataflow])
        flow-processors (rf/subscribe [::subs/current-dataflow-processors])
        delete-dialog? (rf/subscribe [::proc-subs/delete-processor-dialog-open])
        proc-name-edit (rf/subscribe [::proc-subs/edit-name])
        errors    (rf/subscribe [::proc-subs/updating-processor-errors])
        is-updating? (rf/subscribe [::proc-subs/updating-processor])
        proc-errors (rf/subscribe [::subs/current-dataflow-processor-errors])
        has-error? (contains? @proc-errors (keyword (:id @processor)))]
    (style/let [classes proc-styles]
      [:<> 
       (if @delete-dialog? (confirm-delete-processor @processor))
       [:> Box
        [tcom/breadcrumbs (list {:href "#/dataflows" :text "Dataflow Index"}
                                {:href (str "#/dataflows/" (:id @dataflow)) :text (:name @dataflow)}
                                {:href (str "#/processors/" (:id @processor)) :text (:name @processor)})]
        [:div {:class (:delete-processor-wrapper classes)}
         [:> Tooltip {:title "Delete this processor" :placement :left-end}
          [:> IconButton {:class (:delete-processor-button classes)
                          :color :secondary
                          :onClick #(rf/dispatch [::proc-events/toggle-delete-processor-dialog])
                          :size :small}
           [:> DeleteIcon]]]]]
       [tcom/full-content-ui {:title (:name @processor)
                              :editable-title true
                              :edit-opts {:done-event 
                                          (fn [e]
                                            (rf/dispatch-sync [::proc-events/set-processor-edit-field
                                                               :name (-> e .-target .-value)])
                                            (save-processor e @processor))}}

        (if (nil? @processor) [tcom/loading-backdrop])
        [:> Box {:class {:description-wrapper classes}}
         [tcom/editable-text
          :subtitle1
          (:description @processor)
          {:done-event (fn [e]
                         (rf/dispatch-sync [::proc-events/set-processor-edit-field :description (-> e .-target .-value)])
                         (save-processor e @processor))}]]

        (if has-error? [show-processor-errors (get @proc-errors (keyword (:id @processor)))])

        [:> Card {:class (:proc-wrapper classes)}
         (if (some? @processor)
           (do [:form {:onSubmit #(save-processor % @processor)}
                [common-fields processor (:enabled @dataflow)]
                [:> Container
                 [input-fields processor]
                 (case (:type @processor)
                   "SCRIPT" [script-fields processor]
                   "COLLECT" [collect-fields processor]
                   "EXTERNAL" [external-fields processor]
                   [:<>])]
                ]))]]])))

(defn processor-editor
  [id]
  (r/create-class
   {:reagent-render processor-editor*
    :component-did-mount
    (fn []
      (net/auth-dispatch [::events/fetch-processor id {:load-processors? true
                                                       :load-runplan-status? true}]))
    :component-will-unmount
    (fn []
      (rf/dispatch-sync [::proc-events/clear-processor-edit-fields])
      (rf/dispatch-sync [::events/clear-collection-list])
      (rf/dispatch-sync [::events/clear-dataflow-data])
      (rf/dispatch-sync [::events/clear-processor-data]))}))
