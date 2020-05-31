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
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn set-theme [dark-theme?]
  (createMuiTheme (clj->js {:palette {:type (if dark-theme? "dark" "light")}
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
        theme (set-theme dark-theme?)]
    [:> ThemeProvider {:theme theme}
     [:> CssBaseline]
     (main-panel*)]))
