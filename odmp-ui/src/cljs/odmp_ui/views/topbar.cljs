(ns odmp-ui.views.topbar
  (:require [re-frame.core :as rf]
            [breaking-point.core :as bp]
            [semantic-ui-reagent.core :as sui]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]))


(defn topbar []
 (let [sidebar-expanded (rf/subscribe [::subs/sidebar-expanded])]
  [sui/Menu {:className "main-topbar"}
    [sui/MenuItem {:as "a" :on-click #(rf/dispatch [::events/set-sidebar-expanded nil])}
     [sui/Icon {:name (if @sidebar-expanded "chevron left" "chevron right")}]]]))
