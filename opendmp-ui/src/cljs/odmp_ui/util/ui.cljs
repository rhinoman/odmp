(ns odmp-ui.util.ui)

(defn ignore-return [e]
  (if (= (.-keyCode e) 13) (.preventDefault e)))

(defn upper-case [s]
  (cond
      (nil? s) nil
      (string? s) (clojure.string/upper-case s)
      :else s))
