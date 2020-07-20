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

(ns odmp-ui.views.dataflow.flow
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.subs :as subs]
            [odmp-ui.events :as events]
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.data :as dutil]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.subs :as subs]
            [odmp-ui.views.dataflow.dataflow-modals :as d-modals]
            [odmp-ui.views.dataflow.processor-modals :as p-modals]
            [odmp-ui.util.window :as window]
            [odmp-ui.views.dataflow.processor :refer [processor-card]]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Link" :default Link]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/core/Tooltip" :default Tooltip]
            ["@material-ui/icons/AddTwoTone" :default AddIcon]
            ["@material-ui/icons/RemoveTwoTone" :default RemoveIcon]
            ["@material-ui/icons/DeleteTwoTone" :default DeleteIcon]
            ["react-lineto" :default LineTo]))


(defn flow-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:right {:float :right}
     :delete-dataflow-wrapper{:float :right
                              :margin-top 0}
     :description-wrapper {:max-width 600
                           :margin-bottom 20
                           :overflow-wrap :break-word}
     :proc-wrapper {:min-height 400
                    :padding 10
                    :display :flex
                    :position :relative
                    :justify-content :left
                    :background-color "#333"}
     :phase-col {:border-right "1px solid #424242"
                 :padding 5
                 :flex-grow 1
                 :display :flex
                 :flex-flow :column
                 :align-items :center
                 :justify-content :center
                 :text-align :center
                 "&:last-child" {:border-right :none}
                 }
      :phase-header {:position :absolute
                     :top 5
                     :margin-bottom 5}}))

(defn toolbar [num-phases classes]
  [:> Toolbar {:disableGutters true}
   [:> Grid {:container true :spacing 2}
    [:> Grid {:item true :xs 9}]
    [:> Grid {:item true :xs 3}
     [:> Button {:color :primary
                 :variant :contained
                 :disableElevation true
                 :size :small
                 :disabled (= @num-phases 0)
                 :onClick #(swap! num-phases inc)
                 :class (:right classes)}
      [:> AddIcon] "Add Phase"]]]])


(defn connection
  "Draw single connection line"
  [processor]
  (map (fn [i]
         (if (and (= (:sourceType i) "PROCESSOR") (some? (:sourceId i)))
           ^{:key (str "LINK_" (:id processor) "_" (:sourceId i))}
           [:> ^js LineTo {:from (:sourceId i)
                       :to (:id processor)
                       :borderColor "gray"
                       :delay 0
                       :zIndex 5}]))
       (:inputs processor)))

(defn connections
  "Displays lines connecting dependent processors"
  [processors]
  (map connection processors))

(defn phase
  "Displays an individual phase 'column' in the flow"
  [phase-num processors num-phases classes & {:keys [body-text]}]
  ^{:key (str "PHASE_" phase-num)}
  [:div {:class (:phase-col classes)}
   [:> Typography {:variant :subtitle1 :component :h3 :class (:phase-header classes)}
    (str "Phase " phase-num)]
   (map #(processor-card %) processors)
   (connections processors)
   [:> Button {:color :primary
               :onClick #(rf/dispatch [::events/toggle-create-processor-dialog phase-num])
               :size :small} [:> AddIcon] "Add Processor"]
   (if (and (> phase-num 1) (= (count processors) 0))
     [:> Button {:color :secondary
                 :onClick #(swap! num-phases dec)
                 :size :small} [:> RemoveIcon] "Remove Phase"]
     )
   (if (some? body-text) body-text)])

(defn empty-flow
  "What to display when a dataflow has no processors"
  [classes]
  (phase 1 [] 0 classes :body-text [:> Box {:style {:margin-top 5}}
    [:> Typography {:variant :body1} "To start building your flow, create a processor."]
    [:> Typography {:variant :body2 :as :i} "Typically, you'll want to start with an ingest processor."]]))

(defn processor-pane [processors classes]
  (let [num-phases (r/atom (or (dutil/num-phases processors) 1))]
    (fn [processors classes]
      [:<>
       (toolbar num-phases classes)
       [:> Paper {:class (:proc-wrapper classes)}
        (if (= (count processors) 0)
          (empty-flow classes)
          (map (fn [p] (phase p (filter #(= (:phase %) p) processors) num-phases classes))
               (range 1 (inc @num-phases))))]])))

(defn flow
  "Display a dataflow"
  []
  (let [dataflow (rf/subscribe [::subs/current-dataflow])
        processors @(rf/subscribe [::subs/current-dataflow-processors])
        delete-dialog? (rf/subscribe [::d-modals/delete-dataflow-dialog-open])
        create-processor-dialog? (rf/subscribe [::p-modals/create-processor-dialog-open])
        win-size (rf/subscribe [::window/resize])]
    (style/let [classes flow-styles]
      [:<>
       (if @delete-dialog? (d-modals/confirm-delete-dataflow @dataflow))
       (if @create-processor-dialog? (p-modals/create-processor-modal (:id @dataflow)))
       [:> Box [tcom/breadcrumbs (list {:href "#/dataflows" :text "Dataflow Index"}
                               {:href (str "#/dataflows/" (:id @dataflow)) :text (:name @dataflow)})]
        [:div {:class (:delete-dataflow-wrapper classes)}
         [:> Tooltip {:title "Delete this dataflow" :placement :left-end}
          [:> IconButton {:class (:delete-dataflow-button classes)
                          :color :secondary
                          :onClick #(rf/dispatch [::d-modals/toggle-delete-dataflow-dialog])
                          :size :small}
           [:> DeleteIcon]]]]]
       (tcom/full-content-ui
        {:title (:name @dataflow)}
        ;;; :frdw is just to force a redraw when the window is resized
        (if (nil? @dataflow) [tcom/loading-backdrop])
        [:> Box {:class (:description-wrapper classes) :frdw (:width @win-size)}
         [:> Typography {:variant :subtitle1} (:description @dataflow)]]
        
        [processor-pane processors classes])])))
