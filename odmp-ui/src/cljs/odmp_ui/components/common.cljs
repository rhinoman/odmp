(ns odmp-ui.components.common
  (:require 
   [semantic-ui-reagent.core :as sui]))

(defn full-content-ui [{:keys [title]} & children]
  [:div {:style {:padding-left "20px"}}
   [sui/Header [:h2 title]]
   (into [:<>] children)])

;(def full-content-ui* (r/reactify-component full-content-ui))

