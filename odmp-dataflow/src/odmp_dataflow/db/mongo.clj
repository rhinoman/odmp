(ns odmp-dataflow.db.mongo
  (:require [somnium.congomongo :as mg]
            [integrant.core :as ig]
            [environ.core :refer [env]]
            [duct.logger :as logger]))

(defn mongo-url [host port db]
  (str "mongodb://" host ":" port "/" db))

;; Multi method fro translating mongo ObjectId into a hex string
;; Before sending down to the client
(defmulti translate-obj-id class)

(defmethod translate-obj-id clojure.lang.IPersistentMap [m]
  (assoc m :_id (str (:_id m))))

(defmethod translate-obj-id clojure.lang.LazySeq [c]
  (map translate-obj-id c))

(defn wrap-mongo-request
  "Wraps mongo requests.  Translates object ids to strings and provides some exception handling."
  [req logger]
  (try
    (let [response (req)]
      {:status 200 :body (translate-obj-id response)})
    (catch IllegalArgumentException iae
      {:status 400 :body {:error "Illegal Argument"}})
    (catch Exception e
      (throw e))))

(defmethod ig/init-key :odmp-dataflow.db/mongo [_ options]
  
  (let [host (or (env :mongo-host) (:host options))
        port (or (env :mongo-port) (:port options))
        db (or (env :mongo-db) (:db options))
        username (or (env :mongo-username) (:username options))
        password (or (env :mongo-password) (:password options))]
    
    (mg/make-connection db
                        :instances [{:host host :port port}]
                        :username username
                        :password password
                        :auth-source {:mechanism :scram-1 :source "admin"})))

(defmethod ig/halt-key! :odmp-dataflow.db/mongo [_ conn]
  (mg/close-connection conn))
