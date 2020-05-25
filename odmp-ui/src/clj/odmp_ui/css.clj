(ns odmp-ui.css
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [at-import]]))

;; Color Scheme
(def oxford 0x0B132B)
(def space 0x1C2541)
(def apricot 0xFFB86F)
(def honolulu 0x0E6BA8)
(def red 0xBF1A2F)
(def cadet 0x58A4B0)

(defstyles semantic-overrides
   [:.ui.inverted.oxford.menu {:background-color red }])

(defstyles sidebar
  [:.main-sidebar
   {:transition "width 1s ease"
    :position "fixed"
    :border-radius "0px"
    :display "block"
    :left "0px"
    :top "0px"
    :overflow-x "hidden"
    :height "100%"}
   [:.sidebar-brand {:text-align "center"
                     :color "white"
                     :margin-top "10px"
                     :height "45px"
                     }
    [:i {:margin-right "0"}]]
   [:a.item {:padding-top "13px" :padding-bottom "13px" :height "41px"}
    [:i.icon {:float "left" :width "2rem"}]
    [:label {:margin-left "10px"}]]])

(defstyles topbar
  [:.main-topbar {:position "absolute"
                  :top "0px"
                  :width "100%"
                  :border "none"
                  :border-radius 0}])

(defstyles main-content-area
  [:.main-content-area {:display "block"
                        :transition "width 1s ease"}])

(defstyles screen
  (at-import "fomantic/semantic.min.css")
  [:body {:height "100%"}
   [:#app {:height "100vh"}
    semantic-overrides
    sidebar
    main-content-area
    topbar]]
)
