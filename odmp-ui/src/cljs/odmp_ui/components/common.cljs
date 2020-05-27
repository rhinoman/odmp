(ns odmp-ui.components.common)

(defn full-content-ui [{:keys [title]} & children]
  [:div {:style {:padding-left "20px"}}
   [:div [:h2 title]]
   (into [:<>] children)])

;(def full-content-ui* (r/reactify-component full-content-ui))

