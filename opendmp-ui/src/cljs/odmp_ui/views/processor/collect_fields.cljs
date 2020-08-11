;; Copyright 2020 James Adam and the Open Data Management Platform contributors.

;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at

;; http://www.apache.org/licenses/LICENSE-2.0

;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns odmp-ui.views.processor.collect-fields
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [odmp-ui.views.processor.events :as proc-events]
   [odmp-ui.views.processor.subs :as proc-subs]
   [odmp-ui.views.processor.styles :refer [proc-styles]]
   [odmp-ui.subs :as subs]
   [odmp-ui.events :as events]
   [odmp-ui.util.styles :as style]))

(defn collect-fields [processor]
  (let [collections (rf/subscribe [::subs/collections])
        dest-types (rf/subscribe [::subs/lookup-destination-types])]
    (if (nil? @collections) (rf/dispatch [::events/fetch-collection-list]))
    (if (nil? @dest-types) (rf/dispatch [::events/lookup-destination-types]))
    [:div]))

