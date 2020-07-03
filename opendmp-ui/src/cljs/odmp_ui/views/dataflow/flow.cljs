(ns odmp-ui.views.dataflow.flow
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [odmp-ui.util.styles :as style]
            [odmp-ui.components.common :as tcom]
            [odmp-ui.subs :as subs]))


(defn flow-styles [^js/Mui.Theme theme]
  {})

(defn flow []
  (style/let [classes flow-styles]
    (tcom/full-content-ui {:title ""}
      [:div])))
