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

(ns odmp-ui.views.dashboard.home
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]
   [odmp-ui.events :as events]
   [odmp-ui.components.common :as tcom]
   [odmp-ui.util.network :as net]
   [odmp-ui.views.dataflow.index :refer [dataflow-table]]
   ["@material-ui/core/Button" :default Button]
   ["@material-ui/core/Grid" :default Grid]
   ["@material-ui/core/Paper" :default Paper]
   ["@material-ui/core/Typography" :default Typography]))

(defn home-panel* []
  (let [name (rf/subscribe [::subs/name])
        dataflows (rf/subscribe [::subs/dataflows])]
    (tcom/full-content-ui {:title "Dashboard"}
     [:div
      [:> Typography {:variant "h3"} "Open Data Management Platform"]]
     [:> Grid {:container true :spacing 2}
      [:> Grid {:item true :xs 6 :style {:margin 5}}
       [:> Typography {:variant :h5 :style {:margin-top 10}} "Active Dataflows"]
       [:> Paper
        [dataflow-table dataflows]]]
      ]
     [:div])))

(defn home-panel []
  (r/create-class
   {:reagent-render home-panel*
    :component-did-mount 
    (fn []
      (net/auth-dispatch [::events/fetch-dataflow-list {:enabled true}]))
    :component-will-unmount
    (fn []
      (rf/dispatch-sync [::events/clear-dataflow-data]))}))
