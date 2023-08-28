(ns os-test.core
  (:gen-class)
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:require
   [clojure.tools.cli :as cli]
   [clojure.tools.logging :as log]
   [clj-jargon.init :as irods]
   [clojure-commons.config :as config]
   [os-test.amq :as amq]
   [os-test.config :as cfg]
   [qbits.spandex :as s])
  (:import [java.net URL]))

(defn- init-es
  "Establishes a connection to elasticsearch"
  []
  (let [url      (URL. (cfg/es-uri))
        host-map {:hosts [(str url)]}
        opts     (if (or (empty? (cfg/es-user)) (empty? (cfg/es-password)))
                   host-map
                   (merge host-map {:http-client {:basic-auth
                                                  {:user (cfg/es-user)
                                                   :password (cfg/es-password)}}}))
        conn     (try
                   (s/client opts)
                   (catch Exception e (log/debug e) nil))]
    (if conn
      (do
        (log/info (format "Successfully connected to Elasticsearch: %s" url))
        conn)
      (do
        (log/info "Failed to find elasticsearch. Retrying...")
        (Thread/sleep 1000)
        (recur)))))

(defn- init-irods
  []
  (irods/init (cfg/irods-host)
              (str (cfg/irods-port))
              (cfg/irods-user)
              (cfg/irods-pass)
              (cfg/irods-home)
              (cfg/irods-zone)
              (cfg/irods-default-resource)))
