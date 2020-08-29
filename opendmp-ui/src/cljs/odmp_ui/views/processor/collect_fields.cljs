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

(ns odmp-ui.views.processor.collect-fields
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.views.processor.events :as proc-events]
   [odmp-ui.views.processor.subs :as proc-subs]
   [odmp-ui.views.processor.styles :refer [proc-styles]]
   [odmp-ui.subs :as subs]
   [odmp-ui.events :as events]
   [odmp-ui.util.ui :refer [ignore-return]]
   [odmp-ui.util.styles :as style]
   ["@material-ui/core/Box" :default Box]
   ["@material-ui/core/Grid" :default Grid]
   ["@material-ui/core/FormGroup" :default FormGroup]
   ["@material-ui/core/FormControl" :default FormControl]
   ["@material-ui/core/InputLabel" :default InputLabel]
   ["@material-ui/core/Select" :default Select]
   ["@material-ui/core/Typography" :default Typography]
   ["@material-ui/core/MenuItem" :default MenuItem]
   ["@material-ui/core/TextField" :default TextField]))

(defn collect-fields [processor]
  (let [collections (rf/subscribe [::subs/collections])
        dest-types (rf/subscribe [::subs/lookup-destination-types])
        collection (rf/subscribe [::proc-subs/edit-collect-collection])
        dest-type (rf/subscribe [::proc-subs/edit-collect-destination-type])
        location (rf/subscribe [::proc-subs/edit-collect-location])
        prefix   (rf/subscribe [::proc-subs/edit-collect-record-prefix])
        coll-field-value (or @collection
                             (get-in @processor [:properties :collection])
                             "")
        dest-type-field-value (or @dest-type
                                  (get-in @processor [:properties :type])
                                  "NONE")
        loc-field-value (or @location
                            (get-in @processor [:properties :location])
                            "")
        prefix-field-value (or @prefix 
                               (get-in @processor [:properties :prefix])
                               "")]
    (if (nil? @collections) (rf/dispatch [::events/fetch-collection-list]))
    (if (nil? @dest-types) (rf/dispatch [::events/lookup-destination-types]))
    (style/let [classes proc-styles]
      (if (and (some? @dest-types) (some? @collections))
        [:> Box {:style {:margin-top 10}}
         [:> Typography {:variant :subtitle2} "Choose a Collection"]
         [:> Grid {:container true :spacing 2}
          [:> Grid {:item true :xs 3}
           [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
            [:> InputLabel {:id "INPUT_COLLECTION_LABEL"} "Collection"]
            [:> Select {:labelid "INPUT_COLLECTION_LABEL"
                        :value coll-field-value
                        :onChange #(rf/dispatch [::proc-events/set-processor-property :collection (-> % .-target .-value)])}
             [:> MenuItem {:value ""} [:em "NONE"]]
             (map (fn [c] ^{:key (str "INPUT_COLLECTION_" (:id c))}
                    [:> MenuItem {:value (:id c)} (:name c)]) @collections)]]]
          [:> Grid {:item true :xs 9}
           [:> TextField {:margin :dense
                          :variant :filled
                          :required false
                          :fullWidth true
                          :label "Record Prefix (record names will start with this)"
                          :onKeyDown ignore-return
                          :type :text
                          :defaultValue prefix-field-value
                          :onBlur #(rf/dispatch [::proc-events/set-processor-property :prefix (-> % .-target .-value)])}]]]
         [:> Typography {:variant :subtitle2 :style {:marginTop 10}} "Select a destination for the data"]
         [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
          [:> InputLabel {:id "INPUT_DESTINATION_TYPE_LABEL"} "Destination Type"]
          [:> Select {:labelid "INPUT_DESTINATION_TYPE_LABEL"
                      :value dest-type-field-value
                      :onChange #(rf/dispatch [::proc-events/set-processor-property :type (-> % .-target .-value)])}
           (map (fn [dt] ^{:key (str "INPUT_TYPE_" dt)}
                  [:> MenuItem {:value dt} dt]) @dest-types)]]
         [:> TextField {:margin :dense
                        :variant :filled
                        :required true
                        :fullWidth true
                        :label "Location"
                        :onKeyDown ignore-return
                        :type :text
                        :defaultValue loc-field-value
                        :onBlur #(rf/dispatch [::proc-events/set-processor-property :location (-> % .-target .-value)])}]]))))

