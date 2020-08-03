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

(ns odmp-ui.views.processor.styles)

(defn proc-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:edit-processor-wrapper {}
     :delete-processor-wrapper {:float :right
                                :margin-top 0}
     :description-wrapper {:max-width 600
                           :margin-bottom 20
                           :overflow-wrap :break-word}
     :proc-wrapper {:min-height 400
                    :margin-top 10
                    }
     :save-action-button {:margin-right 10
                          :margin-top 5}
     :card-detail-header {}
     :ace-editor-wrapper {:margin-top 10
                          :padding-bottom 10}
     :ace-code-editor {:width "100%"
                       :height "100%"}}))
