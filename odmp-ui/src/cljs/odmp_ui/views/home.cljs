(ns odmp-ui.views.home
  (:require
   [re-frame.core :as re-frame]
   [breaking-point.core :as bp]
   [odmp-ui.subs :as subs]
   [odmp-ui.components.common :as tcom]
   [semantic-ui-reagent.core :as sui]))

(defn display-re-pressed-example []
  (let [re-pressed-example (re-frame/subscribe [::subs/re-pressed-example])]
    [:div
     [sui/Button {:primary true} "I'm a button!"]
     [:p
      [:span "Re-pressed is listening for keydown events. A message will be displayed when you type "]
      [:strong [:code "hello"]]
      [:span ". So go ahead, try it out!"]]

     (when-let [rpe @re-pressed-example]
       [:div
        {:style {:padding          "16px"
                 :background-color "lightgrey"
                 :border           "solid 1px grey"
                 :border-radius    "4px"
                 :margin-top       "16px"
                 }}
        rpe])]))


(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    (tcom/full-content-ui {:title "HOME"}
     [sui/Header
      [:h1 (str "Hello from " @name ". This is the Home Page.")]]
     [:div
      [:a {:href "#/about"}
       "go to About Page"]]

     [display-re-pressed-example]
     [:div
      [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
      [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]
     )))
