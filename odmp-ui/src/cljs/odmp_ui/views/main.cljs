(ns odmp-ui.views.main
  (:require
   [re-frame.core :as re-frame]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]  
   [odmp-ui.config :refer [topbar-height]]
   [semantic-ui-reagent.core :as sui]
   [odmp-ui.views.topbar :refer [topbar]]
   [odmp-ui.views.sidebar :refer [sidebar]]
   [odmp-ui.components.common :as tcom]
   [odmp-ui.views.home :refer [home-panel]]
   ))

;; about

(defn about-panel []
  [:div
   [sui/Header
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

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])
        sidebar-expanded (re-frame/subscribe [::subs/sidebar-expanded])]
     [sui/Container {:fluid true :style {:height "100%"}}
       [sidebar]
       [:div {:className "main-content-area"
              :style {:border "none"
                      :margin-left (if @sidebar-expanded "230px" "60px")
                      :padding-top (+ topbar-height 10)}}
        [topbar]
        (show-panel @active-panel)]]))
