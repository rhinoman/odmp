(ns odmp-dataflow.handler.api
  (:require [reitit.core :as r]
            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.coercion.spec]
            [integrant.core :as ig]
            [odmp-dataflow.handler.schema :as schema]
            [odmp-dataflow.db.flow :refer [lookup get-list]]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.multipart :as multipart]
            [muuntaja.core :as mun]
            [reitit.ring.coercion :as coercion]
            [clojure.spec.alpha :as s]))

(defmethod ig/init-key :odmp-dataflow.handler/api [_ {:keys [db log] :as opts}]
  (ring/ring-handler
    (ring/router
      [
       ["/swagger.json"
        {:get {:no-doc  true
               :swagger {:info     {:title "ODMP Dataflow API"}
                         :basePath "/"}
               :handler (swagger/create-swagger-handler)}}]
       ["/dataflow"
        {:swagger {:tags ["dataflows"]}}
        [""
         {:get {:summary "Get a list of dataflows"
                :responses {200 {:body [::schema/dataflow]}}
                :handler (fn [_] (get-list opts))}}]
        ["/:id"
         {:get {:summary    "Get a single dataflow record by id"
                :parameters {:path {:id string?}}
                :responses {200 {:body ::schema/dataflow}}
                :handler    (fn [{:keys [path-params]}]
                              (lookup (:id path-params) opts))}}]]]
      {:data {:coercion   reitit.coercion.spec/coercion
              :muuntaja   mun/instance
              :middleware [;; query-params & form-params
                           parameters/parameters-middleware
                           ;; content-negotiation
                           muuntaja/format-negotiate-middleware
                           ;; encoding response body
                           muuntaja/format-response-middleware
                           ;; exception handling
                           exception/exception-middleware
                           ;; decoding request body
                           muuntaja/format-request-middleware
                           ;; coercing response bodys
                           coercion/coerce-response-middleware
                           ;; coercing request parameters
                           coercion/coerce-request-middleware
                           ;; multipart
                           multipart/multipart-middleware]}})
    (ring/routes
      (swagger-ui/create-swagger-ui-handler {:path "/api-docs"})
      (ring/create-default-handler))))
