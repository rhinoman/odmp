(ns odmp-ui.views.dataflow.index
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.util.styles :as style]))


(defn dataflow-styles [^js/Mui.Theme theme] {})

(defn dataflow-index []
  (let []
    (style/let [classes dataflow-styles]
      (tcom/full-content-ui {:title "Data Flows"}
        [:div]))))
