(ns odmp-ui.views.sidebar
  (:require
   [re-frame.core :as rf]
   [odmp-ui.subs :as subs]
   [odmp-ui.config :refer [topbar-height]]
   [odmp-ui.subs :as subs]
   ["@material-ui/core/Drawer" :default Drawer]
   ["@material-ui/core/Divider" :default Divider]
   ["@material-ui/core/List" :default List]
   ["@material-ui/core/ListItem" :default ListItem]
   ["@material-ui/core/ListItemIcon" :default ListItemIcon]
   ["@material-ui/core/ListItemText" :default ListItemText]
   ["@material-ui/icons/Search" :default SearchIcon]
   ["@material-ui/icons/CollectionsBookmarkTwoTone" :default BrowseCollectionsIcon]
))

(defn adj-label [txt]
  (if @(rf/subscribe [::subs/sidebar-expanded])
    [:> ListItemText txt]
    nil))

(defn sidebar []
  (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
    [:> Drawer {:variant "permanent"
                :className "main-sidebar"
                :style {:width (if @sidebar-expanded "230px" "50px")}}
     [:div {:class "sidebar-brand"}   
      [:h1 [:i {:name "glass martini"}] (if @sidebar-expanded "ODMP")]]
     [:> Divider]
     [:> List
      [:> ListItem {:as "a" :title "Search"}
       [:> ListItemIcon [:> SearchIcon]]
       (adj-label "Search")]
      [:> ListItem {:as "a" :title "Browse Collections"}
       [:> ListItemIcon [:> BrowseCollectionsIcon]]
       (adj-label "Browse Collections")]
      [:> ListItem {:as "a" :title "My Collections"}
       [:i {:name "favorite"}]
       (adj-label "My Collections")]
      [:> ListItem {:as "a" :title "My Tasks"}
       [:i {:name "tasks"}]
       (adj-label "My Tasks")]
      [:> ListItem {:as "a" :title "Configuration"}
       [:i {:name "wrench"}]
       (adj-label "Configuration")]]]))
