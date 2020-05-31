(ns odmp-ui.components.common
  (:require ["@material-ui/core" :refer [Typography]]))

(defn full-content-ui [{:keys [title]} & children]
  [:div {:style {:paddingLeft "20px"}}
   [:div [:> Typography {:variant "h2" :style {:fontSize 28 :marginTop 10}} title]]
   (into [:<>] children)])
