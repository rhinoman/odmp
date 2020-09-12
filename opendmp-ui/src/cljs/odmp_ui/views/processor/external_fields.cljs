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

(ns odmp-ui.views.processor.external-fields
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.views.processor.events :as proc-events]
   [odmp-ui.views.processor.subs :as proc-subs]
   [odmp-ui.views.processor.styles :refer [proc-styles]]
   [odmp-ui.util.ui :refer [ignore-return]]
   [odmp-ui.util.styles :as style]
   ["@material-ui/core/TextField" :default TextField]))

(defn external-fields [processor]
  (let [command (:command @(rf/subscribe [::proc-subs/edit-properties]))
        timeout (:timeout @(rf/subscribe [::proc-subs/edit-properties]))
        command-field-value (or command
                                (get-in @processor [:properties :command])
                                "")
        timeout-field-value (or timeout
                                (get-in @processor [:properties :timeout])
                                10)]
    [:<>
     [:> TextField {:margin :dense
                    :variant :filled
                    :required true
                    :fullWidth true
                    :label "Command line to execute"
                    :onKeyDown ignore-return
                    :type :text
                    :helperText "Note: Your command must read from stdin and write to stdout"
                    :defaultValue command-field-value
                    :onBlur #(rf/dispatch [::proc-events/set-processor-property :command (-> % .-target .-value)])}]
     
     [:> TextField {:margin :dense
                    :variant :filled
                    :required false
                    :helperText "Maximum time (in seconds) to wait for command to complete"
                    :onKeyDown ignore-return
                    :type :number
                    :label "Timeout"
                    :defaultValue timeout-field-value
                    :onBlur #(rf/dispatch [::proc-events/set-processor-property :timeout (-> % .-target .-value)])}]]))
