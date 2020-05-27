(ns odmp-ui.views.main
  (:require
   [re-frame.core :as rf]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]  
   [odmp-ui.config :refer [topbar-height]]
   [odmp-ui.views.topbar :refer [topbar]]
   [odmp-ui.views.sidebar :refer [sidebar]]
   [odmp-ui.components.common :as tcom]
   [odmp-ui.views.home :refer [home-panel]]
   ["@material-ui/core" :refer [ThemeProvider
                                createMuiTheme
                                CssBaseline
                                Container]]))

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
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn set-theme [dark-theme?]
  (createMuiTheme (clj->js {:palette {:type (if dark-theme? "dark" "light")}
                            :status {:danger "red"}})))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])
        sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])
        dark-theme? @(rf/subscribe [::subs/dark-theme?])]
     [:> ThemeProvider {:theme (set-theme dark-theme?)}
      [:> CssBaseline]
      [:> Container {:maxWidth "xl" :style {:height "100%"}}
       [sidebar]
       [:div {:className "main-content-area"
              :style {:border "none"
                      :margin-left (if @sidebar-expanded "230px" "60px")
                      :padding-top (+ topbar-height 10)}}
        [topbar]
        (show-panel @active-panel)]]]))
