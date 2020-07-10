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
            [odmp-ui.util.styles :as style]
            [odmp-ui.util.data :as dutil]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.subs :as subs]
            [odmp-ui.views.dataflow.processor :refer [processor-card]]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Link" :default Link]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/icons/AddTwoTone" :default AddIcon]))


(defn flow-styles [^js/Mui.Theme theme]
  (let [palette (js->clj (.. theme -palette) :keywordize-keys true)
        p-type (keyword (:type palette))]
    {:right {:float :right}
     :proc-wrapper {:min-height 400
                    :padding 10
                    :display :flex
                    :position :relative
                    :justify-content :left
                    ;:flex-flow :column
                    :background-color "#333"}
     :phase-col {;:border-left "1px solid #424242"
                 :border-right "1px solid #424242"
                 :padding 5
                 :flex-grow 1
                 :display :flex
                 :flex-flow :column
                 :align-items :center
                 :justify-content :center
                 :text-align :center
                 ;:height "100%"
                
                 "&:last-child" {:border-right :none}
                 }
      :phase-header {:position :absolute
                     :top 5
                     :margin-bottom 5}}))

(defn toolbar [classes]
  [:> Toolbar {:disableGutters true}
   [:> Grid {:container true :spacing 2}
    [:> Grid {:item true :xs 9}]
    [:> Grid {:item true :xs 3}
     [:> Button {:color :primary :variant :contained :disableElevation true :size :small :class (:right classes)}
      [:> AddIcon] "Add Phase"]]]])

(defn empty-flow-text []
  [:div
   [:> Typography {:variant :body1} "To start building your flow, create a processor."]
   [:> Typography {:variant :body2 :as :i} "Typically, you'll want to start with an ingest processor."]])

(defn phase [phase-num processors classes]
  ^{:key (str "PHASE_" phase-num)}
  [:div {:class (:phase-col classes)}
   [:> Typography {:variant :subtitle1 :component :h3 :class (:phase-header classes)}
    (str "Phase " phase-num)]
   (map #(processor-card %) processors)
   [:> Button {:color :primary :size :small} [:> AddIcon] "Add Processor"]])

(defn flow []
  (let [dataflow (rf/subscribe [::subs/current-dataflow])
        processors @(rf/subscribe [::subs/current-dataflow-processors])
        num-phases (dutil/num-phases processors)]
    (style/let [classes flow-styles]
      (tcom/full-content-ui {:title (:name @dataflow)}
       [:> Typography {:variant :subtitle1} (:description @dataflow)]
       (toolbar classes)
       [:> Paper {:class (:proc-wrapper classes)}
        (if (= 0 num-phases)
          (empty-flow-text)
          (map (fn [p] (phase p (filter #(= (:phase %) p) processors) classes))
               (range 1 (inc num-phases))))]))))
