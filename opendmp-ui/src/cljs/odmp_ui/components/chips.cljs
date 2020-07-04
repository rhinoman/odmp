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

(ns odmp-ui.components.chips
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string :as string]
            [odmp-ui.util.styles :as style]
            ["@material-ui/core/Chip" :default Chip]))

(defn chip-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:good {:background-color (get-in palette [:success p-type])}
     :degraded {:background-color (get-in palette [:warning p-type])}
     :bad {:background-color (get-in palette [:error p-type])}
     :default {:background-color (get-in palette [:default p-type])}}
    ))

(defn status-chip [status]
  (style/let [classes chip-styles]
    (case (keyword (string/lower-case status))
      :running [:> Chip {:label status :class (:good classes)}]
      :waiting [:> Chip {:label status :class (:degraded classes)}]
      :error [:> Chip {:label status :class (:bad classes)}]
      [:> Chip {:label status :class (:default classes)}])))

(defn health-chip [status]
  (style/let [classes chip-styles]
    (case (keyword (string/lower-case status))
      :ok [:> Chip {:label status :class (:good classes)}]
      :degraded [:> Chip {:label status :class (:degraded classes)}]
      :error [:> Chip {:label status :class (:bad classes)}]
      [:> Chip {:label status :class (:default classes)}])))
