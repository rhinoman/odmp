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

(ns odmp-ui.views.processor.plugin-fields
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.events :as events]
   [odmp-ui.subs :as subs]
   [odmp-ui.util.network :as net]
   [odmp-ui.util.styles :as style]
   [odmp-ui.views.processor.events :as proc-events]
   [odmp-ui.views.processor.subs :as proc-subs]
   ["react-ace" :default AceEditor]
   ["@material-ui/core/Box" :default Box]
   ["@material-ui/core/FormHelperText" :default FormHelperText]
   ["@material-ui/core/TextField" :default TextField]
   ["@material-ui/core/Grid" :default Grid]
   ["@material-ui/core/FormGroup" :default FormGroup]
   ["@material-ui/core/FormControlLabel" :default FormControlLabel]
   ["@material-ui/core/Switch" :default Switch]
   ["@material-ui/core/FormControl" :default FormControl]
   ["@material-ui/core/InputLabel" :default InputLabel]
   ["@material-ui/core/Select" :default Select]
   ["@material-ui/core/Typography" :default Typography]
   ["@material-ui/core/Switch" :default Switch]
   ["@material-ui/core/MenuItem" :default MenuItem]))


(defn text-field
  "A simple text input field"
  [name field value]
  [:> TextField {:type :text
                 :variant :filled
                 :margin :dense
                 :fullWidth true
                 :helperText (:helperText field)
                 :required (:required field)
                 :defaultValue value
                 :onBlur #(rf/dispatch [::proc-events/set-processor-property name (-> % .-target .-value)])
                 :label name}])

(defn number-field
  "A numerical input"
  [name field value]
  [:> TextField {:type :number
                 :variant :filled
                 :margin :dense
                 :helperText (:helperText field)
                 :required (:required field)
                 :defaultValue value
                 :onBlur #(rf/dispatch [::proc-events/set-processor-property name (-> % .-target .-value)])
                 :label name}])

(defn code-field-changed [name value atm]
  (rf/dispatch [::proc-events/set-processor-property name value])
  (reset! atm value))

(defn code-field
  "An input field that displays a code editor"
  [name field value]
  (let [editor-contents (r/atom (or value
                                    ""))]
    (fn [ace-mode]
      [:> Box
       [:> AceEditor {:mode (get-in field [:properties :language])
                      :theme "monokai"
                      :name (str "INPUT_" name)
                      :width "100%"
                      :showPrintMargin false
                      :style {:line-height 1.5}
                      :focus true
                      :fontSize 14
                      :value @editor-contents
                      :onChange #(code-field-changed name % editor-contents)}]
       [:> FormHelperText (:helperText field)]])))

(defn boolean-field [name field value]
  (let [value* (if (= value "") false value)
        switch-state (r/atom (or value*) false)]
   (fn []
     [:> FormControl {:component "fieldset"}
      [:> FormGroup
       [:> FormControlLabel
        {:label name
         :control
         (r/as-element
          [:> Switch {:name name
                      :checked @switch-state
                      :onChange (fn [e] (swap! switch-state not)
                                  (rf/dispatch [::proc-events/set-processor-property name @switch-state]))
                      :color :primary}])}]
       [:> FormHelperText (:helperText field)]]])))

(defn enum-field [name field value]
  [:> Box
   [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
    [:> InputLabel {:id (str "INPUT_PLUGIN_" name "_LABEL")} name]
    [:> Select {:labelid (str "INPUT_PLUGIN_" name "_LABEL")
                :value value
                :onChange #(rf/dispatch [::proc-events/set-processor-property name (-> % .-target .-value)])}
     [:> MenuItem {:value ""} [:em "NONE"]]
     (map (fn [o] ^{:key (str "INPUT_" o "_ITEM")}
            [:> MenuItem {:value o} o]) (:options field))]
    [:> FormHelperText (:helperText field)]]])

(defn plugin-field [name field processor]  
  (let [type (:type field)
        field-value (name @(rf/subscribe [::proc-subs/edit-properties]))
        field-disp-value (or field-value
                             (get-in @processor [:properties name])
                             "")]
    [:> Box {:style {:margin-bottom 5 :margin-top 5}}
     (case type
       "STRING" [text-field name field field-disp-value]
       "NUMBER" [number-field name field field-disp-value]
       "CODE" [code-field name field field-disp-value]
       "BOOLEAN" [boolean-field name field field-disp-value]
       "ENUM" [enum-field name field field-disp-value]
       nil)]))

(defn plugin-fields* [processor]
  (let [config-map (rf/subscribe [::subs/plugin-config])
        configs (vals @config-map)
        service-name (rf/subscribe [::proc-subs/edit-service-name])
        service-name-value (or @service-name
                               (get-in @processor [:properties :serviceName])
                               "")
        plugin-selected (get @config-map (keyword service-name-value))]
    [:<>
     (when configs
       [:> Box {:style {:padding-bottom 10}}
        [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
         [:> InputLabel {:id "INPUT_PLUGIN_LABEL"} "Plugin"]
         [:> Select {:labelid "INPUT_PLUGIN_LABEL"
                     :value service-name-value
                     :onChange #(rf/dispatch [::proc-events/set-processor-property :serviceName (-> % .-target .-value)])}
          [:> MenuItem {:value ""} [:em "NONE"]]
          (map (fn [s] ^{:key (str "INPUT_" (:serviceName s) "_PLUGIN")}
                 [:> MenuItem {:value (:serviceName s)} (:displayName s)]) configs)]]
        (doall (map (fn [[k v]] 
                      ^{:key (str "INPUT_" k "_" (:type v))}
                      [:> Box (plugin-field k v processor)]) (:fields plugin-selected)))])]))

(defn plugin-fields
  "Fields specific to plugin processors"
  [processor]
  (r/create-class
   {:reagent-render (fn [processor] (plugin-fields* processor))
    :component-did-mount
    (fn []
      (net/auth-dispatch [::events/fetch-plugin-config]))
    :component-will-unmount
    (fn [])}))
