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

(ns odmp-ui.components.common
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [odmp-ui.events :as events]
            [odmp-ui.subs :as subs]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Snackbar" :default Snackbar]
            ["@material-ui/lab/Alert" :default Alert]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Backdrop" :default Backdrop]
            ["@material-ui/core/CircularProgress" :default CircularProgress]
            ["@material-ui/core/DialogActions" :default DialogActions]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/Breadcrumbs" :default Breadcrumbs]
            ["@material-ui/core/Link" :default Link]))

(defn snackbar [snackbar-data]
  [:> Snackbar {:open (:open snackbar-data)
                :autoHideDuration 5000
                :onClose #(rf/dispatch [::events/clear-snackbar])
                :anchorOrigin {:horizontal "center" :vertical "top"}}
   [:> Alert {:severity (:severity snackbar-data)} (:text snackbar-data)]])

(defn full-content-ui [{:keys [title]} & children]
  (let [sb (rf/subscribe [::subs/snackbar])]
    [:div {:style {:paddingLeft "20px"}}
     [:div [:> Typography {:variant "h2" :style {:fontSize 28 :marginTop 15 :marginBottom 10}} title]]
     (if (:open @sb)
       [snackbar @sb])
     (into [:<>] children)]))

(defn error-alert [err-result]
  (let [response (:response err-result)
        message  (:message response)
        errors (:errors response)]
    [:> Alert {:severity :error} 
     message
     [:ul
      (map (fn [[k v] m] ^{:key (str k v)} [:li v]) errors)]]))

(defn confirm-dialog
  "Dialog for confirming an action"
  [open-state {:keys [question text confirm-action cancel-action]}]
  [:> Dialog {:open @open-state
              :onClose cancel-action
              :aria-labelledby "confirm-dialog-title"
              :aria-describedby "confirm-dialog-text"}
   [:> DialogTitle {:id "confirm-dialog-title"} question]
   [:> DialogContent
    [:> DialogContentText {:id "confirm-dialog-text"} text]]
   [:> DialogActions
    [:> Button {:color :primary
                :autoFocus true
                :onClick cancel-action} "Cancel"]
    [:> Button {:color :secondary :onClick confirm-action} "Confirm"]]])

(defn breadcrumbs
  "Displays breadcrumb links"
  [links]
  [:> Box {:style {:margin-top 10 :padding-left 20}}
   [:> Breadcrumbs
    (map (fn [link]
           (if (and (some? (:text link)) (some? (:href link)))
                    ^{:key (str "BREADCRUMB_" (:text link))}
                    [:> Link {:href (:href link)
                              :variant :body2} (:text link)]))
             links)]])

(defn loading-backdrop
  "Displays a spinner with a backdrop"
  []
  [:> Backdrop {:open true :style {:zIndex 99}}
   [:> CircularProgress {:color :inherit}]])
