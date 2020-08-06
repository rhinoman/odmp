(ns odmp-ui.util.ui)

(defn ignore-return [e]
  (if (= (.-keyCode e) 13) (.preventDefault e)))
