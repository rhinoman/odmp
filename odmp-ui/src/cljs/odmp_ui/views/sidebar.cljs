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
   ["@material-ui/icons/Search" :default SearchIcon]
   ["@material-ui/icons/FavoriteTwoTone" :default FavoriteTwoToneIcon]
   ["@material-ui/icons/CollectionsBookmarkTwoTone" :default BrowseCollectionsIcon]
   ["@material-ui/icons/DoubleArrowTwoTone" :default DoubleArrowTwoToneIcon]
   ["@material-ui/icons/LocalCafeTwoTone" :default LocalCafeTwoToneIcon]))

(defn sidebar-styles [^js/Mui.Theme theme]
  {:sidebarBrand {:text-align "center"
                  :padding "10px"
                  :margin-bottom 50
                  }
   :sidebarList {:overflowX "hidden"}
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
    [:> ListItemText {:style {:margin 0}} txt]
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
        [:> LocalCafeTwoToneIcon {:fontSize "default"}]
        (if @sidebar-expanded [:span "ODMP"])]]
      
      [:> List {:class [(:sidebarList classes) (if @sidebar-expanded (:sideDrawerOpen classes) (:sideDrawerClose classes))]}
       [:> ListItem {:button true :key "search" :title "Search"}
        [:> ListItemIcon [:> SearchIcon]]
        (adj-label "Search")]
       [:> ListItem {:button true :key "browse" :title "Browse Collections"}
        [:> ListItemIcon [:> BrowseCollectionsIcon]]
        (adj-label "Browse Collections")]
       [:> ListItem {:button true :key "favorites" :title "My Collections"}
        [:> ListItemIcon [:> FavoriteTwoToneIcon]]
        (adj-label "My Collections")]
       [:> ListItem {:button true :key "dataflows" :title "Data Flows"}
        [:> ListItemIcon [:> DoubleArrowTwoToneIcon]]
        (adj-label "Data Flows")]
       ;; [:> ListItem {:as "a" :title "Configuration"}
       ;;  [:i {:name "wrench"}]
       ;;  (adj-label "Configuration")]
       ]])))
