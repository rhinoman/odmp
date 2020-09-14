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

(ns odmp-ui.views.processor.script-fields
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.views.processor.events :as proc-events]
   [odmp-ui.views.processor.subs :as proc-subs]
   [odmp-ui.views.processor.styles :refer [proc-styles]]
   [odmp-ui.util.styles :as style]
   ["react-ace" :default AceEditor]
   ["ace-builds/src-noconflict/mode-python"]
   ["ace-builds/src-noconflict/mode-clojure"]
   ["ace-builds/src-noconflict/mode-plain_text"]
   ["ace-builds/src-noconflict/theme-monokai"]
   ["@material-ui/core/Box" :default Box]
   ["@material-ui/core/Grid" :default Grid]
   ["@material-ui/core/FormGroup" :default FormGroup]
   ["@material-ui/core/FormControl" :default FormControl]
   ["@material-ui/core/InputLabel" :default InputLabel]
   ["@material-ui/core/Select" :default Select]
   ["@material-ui/core/Typography" :default Typography]
   ["@material-ui/core/MenuItem" :default MenuItem]))


(defn code-field-changed [value atm]
  (rf/dispatch [::proc-events/set-processor-property :code value])
  (reset! atm value))

(defn starter-code [ace-mode]
  (case ace-mode
    "clojure" "(defn process [xs])"
    "python" "def process(data):"
    "Select a scripting language"))

(defn code-editor [ace-mode processor]
  (let [editor-contents (r/atom (or (get-in @processor [:properties :code])
                                    (starter-code ace-mode)))]
    (fn [ace-mode processor]  
      [:> AceEditor {:mode ace-mode
                     :theme "monokai"
                     :name "INPUT_SCRIPT_CODE"
                     :width "100%"
                     :showPrintMargin false
                     :focus true
                     :fontSize 14
                     :value @editor-contents
                     :onChange #(code-field-changed % editor-contents)}])))

(defn script-fields [processor]
  (let [script-lang (rf/subscribe [::proc-subs/edit-script-language])
        lang-field-value (or @script-lang
                             (get-in @processor [:properties :language])
                             "")
        ace-mode         (case lang-field-value
                           "CLOJURE" "clojure"
                           "PYTHON"  "python"
                           "plain_text")]
    (style/let [classes proc-styles]
      [:> Box {:style {:margin-top 10}}
       [:> Typography {:variant :subtitle2} "Enter Your script below"]
       [:> Typography
        {:variant :body2}
        "Note: Your script must contain a function named \"process\" that takes a byte array as input and returns a byte array as output"]
       [:> FormControl {:variant :filled :required true :margin :dense :fullWidth true}
        [:> InputLabel {:id "INPUT_LANGUAGE_LABEL"} "Language"]
        [:> Select {:labelid "INPUT_LANGUAGE_LABEL"
                    :value lang-field-value
                    :onChange #(rf/dispatch [::proc-events/set-processor-property :language (-> % .-target .-value)])}
         [:> MenuItem {:value ""} [:em "NONE"]]
         [:> MenuItem {:value "CLOJURE"} "Clojure"]
         [:> MenuItem {:value "PYTHON"} "Python"]]]
       [:> Box {:class (:ace-editor-wrapper classes)}
        [code-editor ace-mode processor]]]))) 
