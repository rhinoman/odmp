(ns odmp-ui.views.sidebar
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.subs :as subs]
   [odmp-ui.config :refer [topbar-height drawer-width]]
   [odmp-ui.subs :as subs]   
   [odmp-ui.util.styles :as style]
   ["@material-ui/core/Drawer" :default Drawer]
   ["@material-ui/core/Divider" :default Divider]
   ["@material-ui/core/List" :default List]
   ["@material-ui/core/ListItem" :default ListItem]
   ["@material-ui/core/ListItemIcon" :default ListItemIcon]
   ["@material-ui/core/ListItemText" :default ListItemText]
   ["@material-ui/core/Typography" :default Typography]   
   ["@material-ui/icons/HomeTwoTone" :default HomeIcon]
   ["@material-ui/icons/SearchTwoTone" :default SearchIcon]
   ["@material-ui/icons/FavoriteTwoTone" :default FavoriteIcon]
   ["@material-ui/icons/CollectionsBookmarkTwoTone" :default BrowseCollectionsIcon]
   ["@material-ui/icons/DoubleArrowTwoTone" :default DoubleArrowIcon]
   ["@material-ui/icons/LocalCafeTwoTone" :default LocalCafeIcon]))

(defn sidebar-styles [^js/Mui.Theme theme]
  {:sidebarBrand {:text-align "center"
                  :padding 10
                  :margin-top 15
                  :margin-bottom 40
                  }
   :sidebarList {:overflowX "hidden"}
   :sidebarListItem {:marginTop 10 :marginBottom 10}
   :sidebarListItemLabel {:margin 0}
   :sidebarBrandHeader {:font-size "24pt"}
   :sideDrawer {:width drawer-width
                :flexShrink 0
                :whiteSpace "nowrap"}
   :sideDrawerOpen {:width drawer-width
                    :transition (.. theme -transitions
                                    (create "width"
                                            #js{:easing (.. theme -transitions -easing -sharp)
                                                :duration (.. theme -transitions -duration -enteringScreen)}))}
   :sideDrawerClose {:transition (.. theme -transitions
                                     (create "width" 
                                             #js{:easing (.. theme -transitions -easing -sharp)
                                                 :duration (.. theme -transitions -duration -leavingScreen)}))
                     :overflowX "hidden"
                     :width (+ (.spacing theme 7) 1)
                     (.breakpoints.up theme "sm") {:width (+(.spacing theme 7) 1)}}
   })


(defn adj-label [txt]
  (if @(rf/subscribe [::subs/sidebar-expanded])
    (style/let [classes sidebar-styles]
      [:> ListItemText {:class (:sidebarListItemLabel classes)} txt])
    nil))

(defn sidebar []
  (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]  
    (style/let [classes sidebar-styles]
     [:> Drawer {:variant "permanent"
                 :class [(:sideDrawer classes)
                         (if @sidebar-expanded
                           (:sideDrawerOpen classes)
                           (:sideDrawerClose classes))]}
      [:div {:class (:sidebarBrand classes)}
       
       [:> Typography {:class (:sidebarBrandHeader classes)
                       :variant "h1"
                       :component "h1"}
        [:> LocalCafeIcon {:fontSize "default"}]
        (if @sidebar-expanded [:span "ODMP"])]]
      
      [:> List {:class [(:sidebarList classes)
                        (if @sidebar-expanded
                          (:sideDrawerOpen classes)
                          (:sideDrawerClose classes))]}
       [:> ListItem {:button true
                     :component "a"
                     :href "/#"
                     :title "Home"
                     :class (:sidebarListItem classes)}
        [:> ListItemIcon [:> HomeIcon]]
        (adj-label "Home")]
       [:> ListItem {:button true
                     :key "search"
                     :title "Search"
                     :class (:sidebarListItem classes)}
        [:> ListItemIcon [:> SearchIcon]]
        (adj-label "Search")]
       [:> ListItem {:button true
                     :key "browse"
                     :title "Browse Collections"
                     :class (:sidebarListItem classes)}
        [:> ListItemIcon [:> BrowseCollectionsIcon]]
        (adj-label "Browse Collections")]
       [:> ListItem {:button true
                     :key "favorites"
                     :title "My Collections"
                     :class (:sidebarListItem classes)}
        [:> ListItemIcon [:> FavoriteIcon]]
        (adj-label "My Collections")]
       [:> ListItem {:button true
                     :component "a"
                     :href "#/dataflows"
                     :key "dataflows"
                     :title "Data Flows"
                     :class (:sidebarListItem classes)}
        [:> ListItemIcon [:> DoubleArrowIcon]]
        (adj-label "Data Flows")]
       ;; [:> ListItem {:as "a" :title "Configuration"}
       ;;  [:i {:name "wrench"}]
       ;;  (adj-label "Configuration")]
       ]])))
