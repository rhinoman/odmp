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

(ns odmp-ui.views.main
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]  
   [odmp-ui.events :as events]
   [odmp-ui.config :refer [topbar-height]]
   [odmp-ui.views.topbar :refer [topbar]]
   [odmp-ui.views.sidebar :refer [sidebar]]
   [odmp-ui.components.common :as tcom]
   [odmp-ui.views.home :refer [home-panel]]
   [odmp-ui.views.dataflow.index :refer [dataflow-index]]
   [odmp-ui.views.dataflow.flow :refer [flow]]
   [odmp-ui.views.processor.edit :refer [processor-editor]]
   [odmp-ui.views.collection.index :refer [collection-index]]
   [odmp-ui.views.collection.collection :refer [collection]]
   [odmp-ui.util.styles :as style]
   ["@material-ui/core" :refer [createMuiTheme
                                useTheme
                                CssBaseline
                                ThemeProvider
                                Container]]))

(defn main-styles [^js/Mui.Theme theme]
  {:mainContent {:flexGrow 1
                 :padding (.spacing theme 3)
                 :position "relative"}})

;; about

(defn about-panel []
  [:div
   [:div
    [:h1 "This is the About Page."]]
   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    :dataflow-index-panel [dataflow-index]
    :dataflow-item-panel [flow]
    :processor-item-panel [processor-editor]
    :collection-index-panel [collection-index]
    :collection-item-panel [collection]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn set-theme [dark-theme?]
  (createMuiTheme (clj->js {:palette {:type (if dark-theme? "dark" "light")
                                      :primary { :main "#00838f" }
                                      :secondary { :main "#f48fb1" }}
                            :status {:danger "red"}})))

(defn main-panel* []
  (let [active-panel (rf/subscribe [::subs/active-panel])
        sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
    (style/let [classes main-styles]
      [:<>
       [topbar]
       [sidebar]
       [:div {:class [(:mainContent classes)]
              :style {:border "none"
                      :padding-top (+ topbar-height 10)}}
        
        (show-panel @active-panel)]])))

(defn main-panel []
  (let [dark-theme? @(rf/subscribe [::subs/dark-theme?])
        auth (rf/subscribe [::subs/authentication])
        theme (set-theme dark-theme?)]
    (if (:authenticated @auth)
      [:> ThemeProvider {:theme theme}
       [:> CssBaseline]
       (main-panel*)])))
