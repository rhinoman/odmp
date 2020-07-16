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

(ns odmp-ui.views.topbar
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [breaking-point.core :as bp]
            [odmp-ui.util.styles :as style]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            [odmp-ui.config :refer [drawer-width]]
            ["@material-ui/core/AppBar" :default AppBar]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Tooltip" :default Tooltip]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/icons/MenuTwoTone" :default MenuTwoToneIcon]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/icons/MenuOpenTwoTone" :default MenuOpenTwoToneIcon]
            ["@material-ui/core/Avatar" :default Avatar]
            ["@material-ui/icons/AccountCircleTwoTone" :default AccountCircleIcon]))

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
                  :zIndex (+ (.. theme -zIndex -drawer) 10)}
   :userInfo {:position :absolute :right 25}
   :username {:position :relative :top "50%" :transform "translateY(-50%)"}})

(defn topbar []
 (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])
       user-info (rf/subscribe [::subs/user-info])]
  (style/let [classes topbar-styles]
    [:> AppBar {:position "fixed"
                :class [(:appBar classes)
                        (if @sidebar-expanded (:appBarShift classes) "")]}
     [:> Toolbar
      [:> IconButton {:on-click #(rf/dispatch [::events/set-sidebar-expanded nil])
                      :class [(:expandButton classes)]}
       (if @sidebar-expanded [:> MenuOpenTwoToneIcon] [:> MenuTwoToneIcon])]
      [:> Box {:class (:userInfo classes)}
       [:> Tooltip {:title (or (:name @user-info) "Not Logged In") :placement :left}
        ;[:> Avatar ]
        [:> AccountCircleIcon {:fontSize :large}]]]]])))
