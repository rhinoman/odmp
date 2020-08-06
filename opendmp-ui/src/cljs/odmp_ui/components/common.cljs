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
            [re-pressed.core :as rp]
            [odmp-ui.subs :as subs]
            [odmp-ui.util.styles :as style]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/TextField" :default TextField]
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

(rf/reg-event-db
 ::toggle-title-edit
 (fn [db [_ _]]
   (assoc db :title-text-being-edited (not (:title-text-being-edited db)))))

(rf/reg-sub
 ::title-edit-state
 (fn [db _]
   (:title-text-being-edited db)))

(defn common-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:title-edit-field {:padding 0}
     :title-edit-field-input {:fontSize 28 :paddingTop 7 :fontWeight 300 :letterSpacing "-0.00833em"}
     :title-header {:fontSize 28 :marginTop 15 :marginBottom 10}}))

(defn snackbar [snackbar-data]
  [:> Snackbar {:open (:open snackbar-data)
                :autoHideDuration 5000
                :onClose #(rf/dispatch [::events/clear-snackbar])
                :anchorOrigin {:horizontal "center" :vertical "top"}}
   [:> Alert {:severity (:severity snackbar-data)} (:text snackbar-data)]])

(defn title-header [title]
  (style/let [classes common-styles]
    [:div
     [:> Typography {:variant "h2" :class (:title-header classes)} title]]))

(defn edit-text-keypress [e state done-event]
  (case (.-keyCode e)
    27 (swap! state not)
    13 (do (done-event e) (swap! state not))
    nil))

(defn editable-title-header [title {:keys [done-event]}]
  (let [edit-state (r/atom false)]
    (fn [title {:keys [done-event]}]   
      (style/let [classes common-styles]
        [:div
         (if @edit-state
           (let [k (rf/subscribe [::subs/keydown-keys])]
             [:> TextField {:variant :standard
                            :margin :dense
                            :autoFocus true
                            :onBlur #(swap! edit-state not)
                            :onKeyDown #(edit-text-keypress % edit-state done-event)
                            :defaultValue title
                            :className (:title-edit-field classes)
                            :InputProps {:classes {:input (str "MuiTypography-root MuiTypography-h2 "
                                                               (:title-edit-field-input classes))}}
                            }])
           [:> Typography {:variant "h2"
                           :class (:title-header classes)
                           :onDoubleClick #(swap! edit-state not)} title])]))))

(defn full-content-ui [{:keys [title editable-title edit-opts]} & children]
  (let [sb (rf/subscribe [::subs/snackbar])]
    [:div {:style {:paddingLeft "20px"}}
     [:div 
      (if editable-title
        [editable-title-header title edit-opts]
        [title-header title])]
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


(defn editable-text [typography-variant text {:keys [done-event]}]
  (let [edit-state (r/atom false)]
    (fn [tv text {:keys [done-event]}]
      (if @edit-state
        [:> TextField {:variant :standard
                       :style {:padding 0 :margin 0}
                       :margin :dense
                       :autoFocus true
                       :onBlur #(swap! edit-state not)
                       :onKeyDown #(edit-text-keypress % edit-state done-event)
                       :defaultValue text
                       :InputProps {:className (str "MuiTypography-root MuiTypography-" (name tv))
                                    :style {:padding-top 0}}}]
        [:> Typography {:variant tv :onDoubleClick #(swap! edit-state not)} text]))))
