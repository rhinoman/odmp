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

(ns odmp-ui.components.icons
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            ["@material-ui/icons/InputTwoTone" :default InputIcon]
            ["@material-ui/icons/CloudUploadTwoTone" :default UploadIcon]
            ["@material-ui/icons/CodeTwoTone" :default CodeIcon]
            ["@material-ui/icons/TransformTwoTone" :default TransformIcon]
            ["@material-ui/icons/WorkTwoTone" :default WorkIcon]
            ["@material-ui/icons/MergeTypeTwoTone" :default MergeIcon]
            ["@material-ui/icons/HelpTwoTone" :default HelpIcon]))

(defn processor-type-icon [processor-type]
  (case processor-type
    "INGEST" [:> InputIcon]
    "EXPORT" [:> UploadIcon]
    "TRANSFORM" [:> TransformIcon]
    "SCRIPT" [:> CodeIcon]
    "AGGREGATOR" [:> MergeIcon]
    "EXTERNAL" [:> WorkIcon]
    [:> HelpIcon]))

(defn processor-type-icon* [processor-type]
  (r/reactify-component processor-type-icon))
