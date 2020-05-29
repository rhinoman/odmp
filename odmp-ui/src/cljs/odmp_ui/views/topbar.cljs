(ns odmp-ui.views.topbar
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [breaking-point.core :as bp]
            [odmp-ui.util.styles :as style]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            [odmp-ui.config :refer [drawer-width]]
            ["@material-ui/core/AppBar" :default AppBar]
            ["@material-ui/core/Toolbar" :default Toolbar]

))

(defn topbar-styles [^js/Mui.Theme theme]
  {:appBar {:width (str "calc(100% - " (.spacing theme 7) "px)")
            ;:zIndex (+ (.. theme -zIndex -drawer) 1)
            :transition (.. theme -transitions
                            (create "width"
                                    #js{:easing (.. theme -transitions -easing -sharp)
                                        :duration (.. theme -transitions -duration -leavingScreen)}))}
   :appBarShift {:marginLeft drawer-width
                 ;:width (str "calc(100% - " drawer-width "px)")
                 :transition (.. theme -transitions
                                 (create "width"
                                         #js{:easing (.. theme -transitions -easing -sharp)
                                             :duration (.. theme -transitions -duration -enteringScreen)}))}})

(defn topbar []
 (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
  (style/let [classes topbar-styles]
    [:> AppBar {:position "fixed"
                :class [(:appBar classes)
                        (if @sidebar-expanded (:appBarShift classes) "")]}
     [:> Toolbar
      ]])))
