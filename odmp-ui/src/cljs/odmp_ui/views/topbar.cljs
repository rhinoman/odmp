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
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/icons/MenuTwoTone" :default MenuTwoToneIcon]
            ["@material-ui/icons/MenuOpenTwoTone" :default MenuOpenTwoToneIcon]))

;; Topbar styles
(defn topbar-styles [^js/Mui.Theme theme]
  {:appBar {:width (str "calc(100% - " (.spacing theme 7) "px)")
            :transition (.. theme -transitions
                            (create "width"
                                    #js{:easing (.. theme -transitions -easing -sharp)
                                        :duration (.. theme -transitions -duration -leavingScreen)}))}
   :appBarShift {:marginLeft drawer-width
                 :width (str "calc(100% - " drawer-width "px)")
                 :transition (.. theme -transitions
                                 (create "width"
                                         #js{:easing (.. theme -transitions -easing -sharp)
                                             :duration (.. theme -transitions -duration -enteringScreen)}))}
   :expandButton {:left (- (.spacing theme 3))
                  :zIndex (+ (.. theme -zIndex -drawer) 10)}})

(defn topbar []
 (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
  (style/let [classes topbar-styles]
    [:> AppBar {:position "fixed"
                :class [(:appBar classes)
                        (if @sidebar-expanded (:appBarShift classes) "")]}
     [:> Toolbar
      [:> IconButton {:on-click #(rf/dispatch [::events/set-sidebar-expanded nil])
                      :class [(:expandButton classes)]}
       (if @sidebar-expanded [:> MenuOpenTwoToneIcon] [:> MenuTwoToneIcon])]
      ]])))
