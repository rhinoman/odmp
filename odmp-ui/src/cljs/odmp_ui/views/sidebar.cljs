(ns odmp-ui.views.sidebar
  (:require
   [re-frame.core :as rf]
   [odmp-ui.subs :as subs]
   ["semantic-ui-react" :as sur]
   [odmp-ui.config :refer [topbar-height]]
   [odmp-ui.subs :as subs]
   [semantic-ui-reagent.core :as sui]))

(defn adj-label [txt]
  (if @(rf/subscribe [::subs/sidebar-expanded])
    [:label txt]
    nil))

(defn sidebar []
  (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
    [sui/Menu {:inverted true
               :vertical true
                                        ;:color "blue"
               :compact (not @sidebar-expanded)
               :icon (not @sidebar-expanded)
               :borderless true
               :className "main-sidebar"
               :style {:width (if @sidebar-expanded "230px" "50px")}}
     [sui/Header {:class "sidebar-brand"}   
      [:h1 [sui/Icon {:name "glass martini"}] (if @sidebar-expanded "TDMP")]]
     [sui/MenuItem {:as "a" :title "Home"}
      [sui/Icon {:name "home"}]
      (adj-label "Home")]
     [sui/MenuItem {:as "a" :title "Search"}
      [sui/Icon {:name "search"}]
      (adj-label "Search")]
     [sui/MenuItem {:as "a" :title "Browse Collections"}
      [sui/Icon {:name "browser"}]
      (adj-label "Browse Collections")]
     [sui/MenuItem {:as "a" :title "My Collections"}
      [sui/Icon {:name "favorite"}]
      (adj-label "My Collections")]
     [sui/MenuItem {:as "a" :title "My Tasks"}
      [sui/Icon {:name "tasks"}]
      (adj-label "My Tasks")]
     [sui/MenuItem {:as "a" :title "Configuration"}
      [sui/Icon {:name "wrench"}]
      (adj-label "Configuration")]]))
