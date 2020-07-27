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

(ns odmp-ui.views.processor.input-fields
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.events :as events]
            [odmp-ui.views.processor.events :as proc-events]
            [odmp-ui.views.processor.subs :as proc-subs]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/FormGroup" :default FormGroup]
            ["@material-ui/core/FormControl" :default FormControl]
            ["@material-ui/core/InputLabel" :default InputLabel]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/TextField" :default TextField]))


(defn text-input-location [idx field]
  (let [edit-inputs (rf/subscribe [::proc-subs/edit-inputs])
        loc-field-value (or (get-in @edit-inputs [idx :sourceLocation])
                                    (:sourceLocation field)
                                    "")]
    [:> TextField {:margin :dense
                   :variant :filled
                   :required true
                   :fullWidth true
                   :label "Location"
                   :type :text
                   :value loc-field-value
                   :onChange #(rf/dispatch [::proc-events/set-processor-input-location-field idx (-> % .-target .-value)])}]))

(defn processor-input-location
  "Location select for a processor input"
  [idx field processor flow-processors]
  (let [edit-inputs (rf/subscribe [::proc-subs/edit-inputs])
        proc-candidates (filter #(< (:phase %) (:phase processor)) flow-processors)
        loc-field-value (or (get-in @edit-inputs [idx :sourceLocation])
                            (:sourceLocation field)
                            "")]
    [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
     [:> InputLabel {:id (str "INPUT_" idx "_LOC_LABEL")} "Processor Source"]
     [:> Select {:labelid (str "INPUT_" idx "_LOC_LABEL")
                 :value loc-field-value
                 :onChange #(rf/dispatch [::proc-events/set-processor-input-location-field idx (-> % .-target .-value)])}
      [:> MenuItem {:value ""} [:em "None"]]
      (map (fn [p] ^{:key (str "INPUT_" idx "_LOC_" (:id p))}
             [:> MenuItem {:value (:id p)} (:name p)]) proc-candidates)]]))

(defn input-field
  "Displays an individual input field"
  [idx processor field]
  (let [flow-processors (rf/subscribe [::subs/current-dataflow-processors])
        edit-inputs (rf/subscribe [::proc-subs/edit-inputs])
        source-types (rf/subscribe [::subs/lookup-source-types])
        type-field-value (or (get-in @edit-inputs [idx :sourceType])
                             (:sourceType field)
                             "NONE")]
    [:> Box
     [:> Grid {:container true :spacing 2}
      [:> Grid {:item true :xs 3}
       [:> FormControl {:variant :filled :margin :dense :fullWidth true}
        [:> InputLabel {:id (str "INPUT_" idx "_LABEL")} "Source Type"]
        [:> Select {:labelid (str "INPUT_" idx "_LABEL")
                    :value type-field-value
                    :onChange #(do 
                                 (rf/dispatch [::proc-events/set-processor-input-type-field idx (-> % .-target .-value)])
                                 (rf/dispatch [::proc-events/set-processor-input-location-field idx ""]))}
         [:> MenuItem {:value "NONE"} [:em "None"]]
         (map (fn [st] ^{:key (str "INPUT_" idx "_ST_" st)}
                [:> MenuItem {:value st} st]) @source-types)]]]
      [:> Grid {:item true :xs 9}
       (case type-field-value
         "NONE" [:> FormControl {:variant :filled :margin :dense :fullWidth true}
                 [:> InputLabel "Select a Source Type"]]
         "PROCESSOR" [processor-input-location idx field processor @flow-processors]
         [text-input-location idx field])]]]))

(defn input-fields [processor]
  (let [source-types (rf/subscribe [::subs/lookup-source-types])
        flow-processors (rf/subscribe [::subs/current-dataflow-processors])
        edit-inputs (rf/subscribe [::proc-subs/edit-inputs])
        num-inputs (count (:inputs @processor))]
    (if (and (some? @source-types) (some? @flow-processors))
      [:> Box
       (doall (map-indexed (fn [idx itm] ^{:key (str (:id processor) "_INPUT_FIELD_" idx)}
                             [input-field idx @processor itm])
                           (:inputs @processor)))
       ;; And one more input field for adding new inputs
       ^{:key (str (:id processor) "_INPUT_FIELD_NEW")}
       [input-field (if (= num-inputs 0) 0 num-inputs) @processor nil]])))
