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

(ns odmp-ui.views.dataflow.processor
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.util.styles :as style]
            [odmp-ui.components.icons :refer [processor-type-icon]]
            [odmp-ui.events :as events]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/CardHeader" :default CardHeader]))

(defn processor-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:proc-card {:width 300
                 :max-height 250
                 :overflow-y :none
                 :margin 5
                 :zIndex 99
                 :text-align :left
                 :border-width :thin
                 :border-style :solid
                 :border-color (get-in palette [:background :paper])
                 "&:hover" {:cursor :pointer
                            :border-color (get-in palette [:primary :contrastText])
                            :border-width :thin
                            :border-style :solid}}}))

(defn processor-card [processor]
  ^{:key (:id processor)}
  [:<>
   (style/let [classes processor-styles]
     [:> Card {:class [(:proc-card classes) (:id processor)]
               :onClick #(events/navigate (str "/processors/" (:id processor)))}
      [:> CardHeader {:title (:name processor)
                      :avatar (r/as-element (processor-type-icon (:type processor)))
                      :subheader (:description processor)}]])])
