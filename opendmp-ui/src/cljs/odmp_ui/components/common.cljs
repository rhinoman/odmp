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

(ns odmp-ui.components.common
  (:require ["@material-ui/core" :refer [Typography]]))

(defn full-content-ui [{:keys [title]} & children]
  [:div {:style {:paddingLeft "20px"}}
   [:div [:> Typography {:variant "h2" :style {:fontSize 28 :marginTop 10}} title]]
   (into [:<>] children)])
