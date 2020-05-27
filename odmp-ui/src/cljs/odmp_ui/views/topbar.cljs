(ns odmp-ui.views.topbar
  (:require [re-frame.core :as rf]
            [breaking-point.core :as bp]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]))


(defn topbar []
 (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
  [:div {:className "main-topbar"}
    [:div {:as "a" :on-click #(rf/dispatch [::events/set-sidebar-expanded nil])}
     [:i {:name (if @sidebar-expanded "chevron left" "chevron right")}]]]))
